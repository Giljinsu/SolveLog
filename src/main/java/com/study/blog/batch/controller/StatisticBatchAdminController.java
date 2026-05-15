package com.study.blog.batch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticBatchAdminController {

    private final JobLauncher jobLauncher;
    private final Job statisticJob;

//    @PostMapping("/api/batch/runStatisticJob")
//    public ResponseEntity<String> runStatisticBatch(@RequestParam int year) {
//        try {
//            JobParameters jobParameters = new JobParametersBuilder()
//                .addLong("runTime", System.currentTimeMillis())
//                .addLong("year", (long) year)
//                .toJobParameters();
//
//            JobExecution jobExecution = jobLauncher.run(statisticJob, jobParameters);
//
//            return ResponseEntity.ok("통계 배치 실행 완료" + jobExecution.getStatus());
//        } catch (Exception e) {
//            log.error("통계 배치 수동 실행 실패", e);
//            return ResponseEntity.internalServerError()
//                .body("통계 배치 실행 실패");
//        }
//    }

}
