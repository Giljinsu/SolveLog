package com.study.blog.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job statisticJob;

    @Scheduled(cron = "0 0 0 * * *")
    public void runStatisticJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(statisticJob, jobParameters);

        } catch (Exception e) {
            throw new RuntimeException("통계 배치 실행 실패", e);
        }
    }
}
