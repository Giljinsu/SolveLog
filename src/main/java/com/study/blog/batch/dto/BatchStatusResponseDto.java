package com.study.blog.batch.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class BatchStatusResponseDto {
    private final String jobName;
    private final String status; // BatchStatus.toString() - COMPLETED / FAILED / STARTED 등
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Long durationSeconds; // startTime/endTime 둘 다 있을 때만 계산
    private final boolean running;

    public BatchStatusResponseDto(String jobName, String status, LocalDateTime startTime,
        LocalDateTime endTime, Long durationSeconds, boolean running) {
        this.jobName = jobName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
        this.running = running;
    }
}
