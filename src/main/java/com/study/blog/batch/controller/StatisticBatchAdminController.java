package com.study.blog.batch.controller;

import com.study.blog.batch.dto.BatchStatusResponseDto;
import com.study.blog.batch.service.StatisticBatchRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 - SecurityConfig에서 /api/admin/** 는 ADMIN만 접근 가능
@Slf4j
@RestController
@RequestMapping("/api/admin/batch/statistic")
@RequiredArgsConstructor
public class StatisticBatchAdminController {

    private final StatisticBatchRunner statisticBatchRunner;

    @GetMapping
    public ResponseEntity<BatchStatusResponseDto> getStatus() {
        return ResponseEntity.ok(statisticBatchRunner.getLatestStatus());
    }

    @PostMapping("/run")
    public ResponseEntity<Void> run() {
        // 이미 실행 중이면 statisticBatchRunner.run()이 BatchJobAlreadyRunningException을 던지고
        // GlobalExceptionHandler가 409로 응답한다.
        statisticBatchRunner.run();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
