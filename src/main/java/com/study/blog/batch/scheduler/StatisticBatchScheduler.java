package com.study.blog.batch.scheduler;

import com.study.blog.batch.service.StatisticBatchRunner;
import com.study.blog.exception.BatchJobAlreadyRunningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticBatchScheduler {

    private final StatisticBatchRunner statisticBatchRunner;

    @Scheduled(cron = "0 0 0 * * *")
    public void runStatisticJob() {
        try {
            statisticBatchRunner.run();
        } catch (BatchJobAlreadyRunningException e) {
            // 관리자 수동 실행 등과 겹친 경우 - 스케줄 실행은 건너뛴다.
            log.warn("통계 배치 스케줄 실행 스킵 - 이미 실행 중: {}", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("통계 배치 실행 실패", e);
        }
    }
}
