package com.study.blog.admin.dto;

import com.study.blog.ai.monitoring.domain.AiAnalysisStatus;
import com.study.blog.ai.monitoring.domain.ErrorSeverity;
import com.study.blog.ai.monitoring.domain.IssueStatus;
import java.time.LocalDateTime;
import lombok.Getter;

// 관리자 Monitoring 목록 응답 (이번 지시 §20)
@Getter
public class AdminErrorIssueResponseDto {
    private final Long issueId;
    private final String exceptionClass;
    private final String representativeMessage;
    private final ErrorSeverity severity;
    private final IssueStatus status;
    private final long occurrenceCount;
    private final LocalDateTime firstOccurredAt;
    private final LocalDateTime lastOccurredAt;
    private final AiAnalysisStatus aiAnalysisStatus;

    public AdminErrorIssueResponseDto(Long issueId, String exceptionClass, String representativeMessage,
        ErrorSeverity severity, IssueStatus status, long occurrenceCount, LocalDateTime firstOccurredAt,
        LocalDateTime lastOccurredAt, AiAnalysisStatus aiAnalysisStatus) {
        this.issueId = issueId;
        this.exceptionClass = exceptionClass;
        this.representativeMessage = representativeMessage;
        this.severity = severity;
        this.status = status;
        this.occurrenceCount = occurrenceCount;
        this.firstOccurredAt = firstOccurredAt;
        this.lastOccurredAt = lastOccurredAt;
        this.aiAnalysisStatus = aiAnalysisStatus;
    }
}
