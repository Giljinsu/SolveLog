package com.study.blog.batch.service;

import com.study.blog.batch.dto.BatchStatusResponseDto;
import com.study.blog.exception.BatchJobAlreadyRunningException;
import com.study.blog.ai.monitoring.dto.ErrorCaptureContext;
import com.study.blog.ai.monitoring.service.ErrorMonitoringFacade;
import com.study.blog.ai.monitoring.trace.TraceIdFilter;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.stereotype.Component;

// statisticJob 실행 + 상태 조회를 한 곳에 모아 Scheduler/관리자 수동 실행이 동일한
// 중복 실행 방지 체크 로직을 공유하도록 한다. 새 Batch Metadata 테이블은 만들지 않고
// 기존 BATCH_JOB_* 테이블을 JobExplorer로 조회한다.
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticBatchRunner {

    private static final String JOB_NAME = "statisticJob";

    private final JobLauncher jobLauncher;
    private final Job statisticJob;
    private final JobExplorer jobExplorer;
    private final ErrorMonitoringFacade errorMonitoringFacade;

    // 이미 실행 중이면 BatchJobAlreadyRunningException을 던지고 새 Job을 실행하지 않는다.
    // isRunning() 사전 체크와 실제 실행 사이에는 미세한 race window가 있다. 이 클래스는
    // 항상 새로운 timestamp 파라미터로만 실행을 시도하므로, 그 짧은 window에서 실제로
    // 충돌이 나면 jobLauncher.run()이 아래 둘 중 하나를 던진다.
    //  - JobExecutionAlreadyRunningException: 같은 timestamp로 동시에 launch되어 같은
    //    JobInstance가 이미 STARTED 상태인 경우
    //  - JobInstanceAlreadyCompleteException: 같은 timestamp로 거의 동시에 실행된 다른
    //    요청이 먼저 시작해 이미 COMPLETED까지 끝나버린 경우 (Job이 1초 이내로 끝나므로
    //    실제로 race 테스트에서 관측됨)
    // 이 둘만 "중복/충돌 실행"으로 보고 409로 변환한다.
    // JobRestartException(재시작 불가 등 설정 문제), JobParametersInvalidException(파라미터
    // 검증 실패 - 현재 statisticJob에는 validator가 없어 실제로는 발생하지 않음) 등 나머지
    // JobExecutionException 하위 예외는 중복 실행과 무관한 실제 오류이므로 409로 바꾸지
    // 않고 그대로 감싸서 500으로 전달되게 한다.
    public JobExecution run() {
        if (isRunning()) {
            throw new BatchJobAlreadyRunningException("이미 실행 중인 통계 Batch가 있습니다.");
        }

        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("runTime", System.currentTimeMillis())
            .toJobParameters();

        try {
            return jobLauncher.run(statisticJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException e) {
            log.warn("통계 배치 실행 충돌 - 이미 실행 중이거나 직전에 실행 완료됨: {}", e.getMessage());
            throw new BatchJobAlreadyRunningException("이미 실행 중인 통계 Batch가 있습니다.");
        } catch (JobExecutionException e) {
            // JobRestartException, JobParametersInvalidException 등 - 중복 실행과는 무관한
            // 실제 Batch 실행 오류이므로 원인을 숨기지 않고 그대로 전달한다.
            log.error("통계 배치 실행 실패", e);
            // Batch 실패는 HTTP 요청/응답 흐름을 거치지 않아 GlobalExceptionHandler가 볼 수
            // 없으므로 여기서 직접 Error Monitoring에 보고한다(analysis.md §14, 이번 지시 §18).
            errorMonitoringFacade.report(e,
                ErrorCaptureContext.forBatch(JOB_NAME, MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY)));
            throw new RuntimeException("통계 배치 실행 실패", e);
        }
    }

    public boolean isRunning() {
        return !jobExplorer.findRunningJobExecutions(JOB_NAME).isEmpty();
    }

    public BatchStatusResponseDto getLatestStatus() {
        boolean running = isRunning();

        JobInstance lastInstance = jobExplorer.getLastJobInstance(JOB_NAME);
        if (lastInstance == null) {
            return new BatchStatusResponseDto(JOB_NAME, null, null, null, null, running);
        }

        JobExecution lastExecution = jobExplorer.getLastJobExecution(lastInstance);
        if (lastExecution == null) {
            return new BatchStatusResponseDto(JOB_NAME, null, null, null, null, running);
        }

        var startTime = lastExecution.getStartTime();
        var endTime = lastExecution.getEndTime();
        Long durationSeconds = (startTime != null && endTime != null)
            ? Duration.between(startTime, endTime).getSeconds()
            : null;

        return new BatchStatusResponseDto(
            JOB_NAME,
            lastExecution.getStatus().toString(),
            startTime,
            endTime,
            durationSeconds,
            running
        );
    }
}
