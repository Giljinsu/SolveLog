package com.study.blog.ai.monitoring.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ErrorIssue 1건에 대한 개별 발생 기록. 보존 기간 경과 후 정리 배치가 삭제한다(§5) - ErrorIssue는 삭제하지 않는다.
@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class ErrorEvent {

    @Id @GeneratedValue
    @Column(name = "event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private ErrorIssue issue;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "trace_id", length = 36)
    private String traceId;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "user_id")
    private Long userId;

    private String username;

    private String ip;

    @Column(name = "stack_trace_excerpt", columnDefinition = "TEXT")
    private String stackTraceExcerpt;

    public static ErrorEvent create(ErrorIssue issue, LocalDateTime occurredAt, String traceId,
        String requestUri, String httpMethod, Integer httpStatus, Long userId, String username,
        String ip, String stackTraceExcerpt) {
        ErrorEvent event = new ErrorEvent();
        event.setIssue(issue);
        event.setOccurredAt(occurredAt);
        event.setTraceId(traceId);
        event.setRequestUri(requestUri);
        event.setHttpMethod(httpMethod);
        event.setHttpStatus(httpStatus);
        event.setUserId(userId);
        event.setUsername(username);
        event.setIp(ip);
        event.setStackTraceExcerpt(stackTraceExcerpt);
        return event;
    }
}
