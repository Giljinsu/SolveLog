package com.study.blog.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.study.blog.exception.BatchJobAlreadyRunningException;
import com.study.blog.ai.monitoring.service.ErrorMonitoringFacade;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

// StatisticBatchRunner.run()이 jobLauncher.run() 시점의 예외 종류에 따라
// 실제로 "중복/충돌 실행"에 해당하는 경우에만 409(BatchJobAlreadyRunningException)로
// 변환하고, 그 외 실제 오류는 그대로 전파하는지 검증한다.
@ExtendWith(MockitoExtension.class)
class StatisticBatchRunnerTest {

    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private Job statisticJob;
    @Mock
    private JobExplorer jobExplorer;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private ErrorMonitoringFacade errorMonitoringFacade;

    private StatisticBatchRunner statisticBatchRunner;

    @BeforeEach
    void setUp() {
        statisticBatchRunner =
            new StatisticBatchRunner(jobLauncher, statisticJob, jobExplorer, errorMonitoringFacade);
    }

    @Test
    void isRunning이_true이면_launch_시도_없이_바로_409예외() throws Exception {
        when(jobExplorer.findRunningJobExecutions("statisticJob"))
            .thenReturn(Set.of(jobExecution));

        assertThatThrownBy(() -> statisticBatchRunner.run())
            .isInstanceOf(BatchJobAlreadyRunningException.class);

        // 이미 실행 중이므로 launch 자체를 시도하지 않아야 한다.
        org.mockito.Mockito.verifyNoInteractions(jobLauncher);
    }

    @Test
    void JobExecutionAlreadyRunningException은_409로_변환된다() throws Exception {
        when(jobExplorer.findRunningJobExecutions("statisticJob")).thenReturn(Set.of());
        when(jobLauncher.run(any(), any()))
            .thenThrow(new JobExecutionAlreadyRunningException("already running"));

        assertThatThrownBy(() -> statisticBatchRunner.run())
            .isInstanceOf(BatchJobAlreadyRunningException.class);

        // 중복 실행은 예상된 흐름이므로 Error Monitoring에 보고하지 않는다.
        org.mockito.Mockito.verifyNoInteractions(errorMonitoringFacade);
    }

    @Test
    void JobInstanceAlreadyCompleteException은_409로_변환된다() throws Exception {
        when(jobExplorer.findRunningJobExecutions("statisticJob")).thenReturn(Set.of());
        when(jobLauncher.run(any(), any()))
            .thenThrow(new JobInstanceAlreadyCompleteException("already complete"));

        assertThatThrownBy(() -> statisticBatchRunner.run())
            .isInstanceOf(BatchJobAlreadyRunningException.class);

        org.mockito.Mockito.verifyNoInteractions(errorMonitoringFacade);
    }

    @Test
    void JobRestartException은_409로_바뀌지_않고_그대로_전달되며_Monitoring에_보고된다() throws Exception {
        when(jobExplorer.findRunningJobExecutions("statisticJob")).thenReturn(Set.of());
        when(jobLauncher.run(any(), any()))
            .thenThrow(new JobRestartException("not restartable"));

        assertThatThrownBy(() -> statisticBatchRunner.run())
            .isNotInstanceOf(BatchJobAlreadyRunningException.class)
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(JobRestartException.class);

        // 중복 실행과 무관한 실제 Batch 오류는 Error Monitoring에 보고되어야 한다(이번 지시 §18).
        org.mockito.Mockito.verify(errorMonitoringFacade).report(any(), any());
    }

    @Test
    void JobParametersInvalidException은_409로_바뀌지_않고_그대로_전달되며_Monitoring에_보고된다() throws Exception {
        when(jobExplorer.findRunningJobExecutions("statisticJob")).thenReturn(Set.of());
        when(jobLauncher.run(any(), any()))
            .thenThrow(new JobParametersInvalidException("invalid parameters"));

        assertThatThrownBy(() -> statisticBatchRunner.run())
            .isNotInstanceOf(BatchJobAlreadyRunningException.class)
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(JobParametersInvalidException.class);

        org.mockito.Mockito.verify(errorMonitoringFacade).report(any(), any());
    }

    @Test
    void 정상_실행시_JobExecution을_그대로_반환한다() throws Exception {
        when(jobExplorer.findRunningJobExecutions("statisticJob")).thenReturn(Set.of());
        when(jobLauncher.run(any(), any())).thenReturn(jobExecution);

        JobExecution result = statisticBatchRunner.run();

        assertThat(result).isEqualTo(jobExecution);
    }
}
