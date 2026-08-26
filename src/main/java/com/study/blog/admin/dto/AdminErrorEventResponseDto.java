package com.study.blog.admin.dto;

import java.time.LocalDateTime;
import lombok.Getter;

// 관리자 Monitoring 상세 - 최근 ErrorEvent 목록 응답 (이번 지시 §20)
@Getter
public class AdminErrorEventResponseDto {
    private final Long eventId;
    private final LocalDateTime occurredAt;
    private final String traceId;
    private final String requestUri;
    private final String httpMethod;
    private final Integer httpStatus;
    private final Long userId;
    private final String username;
    private final String ip;
    private final String stackTraceExcerpt;

    public AdminErrorEventResponseDto(Long eventId, LocalDateTime occurredAt, String traceId,
        String requestUri, String httpMethod, Integer httpStatus, Long userId, String username,
        String ip, String stackTraceExcerpt) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.traceId = traceId;
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.httpStatus = httpStatus;
        this.userId = userId;
        this.username = username;
        this.ip = ip;
        this.stackTraceExcerpt = stackTraceExcerpt;
    }
}
