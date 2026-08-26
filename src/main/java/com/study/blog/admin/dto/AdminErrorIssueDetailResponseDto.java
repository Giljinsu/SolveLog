package com.study.blog.admin.dto;

import com.study.blog.ai.monitoring.domain.AiAnalysisStatus;
import com.study.blog.ai.monitoring.domain.ErrorSeverity;
import com.study.blog.ai.monitoring.domain.IssueStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

// 관리자 Monitoring 상세 응답 - Issue 정보 + AI 분석 결과 (이번 지시 §20)
@Getter
public class AdminErrorIssueDetailResponseDto {
    private final Long issueId;
    private final String exceptionClass;
    private final String representativeMessage;
    private final ErrorSeverity severity;
    private final IssueStatus status;
    private final long occurrenceCount;
    private final LocalDateTime firstOccurredAt;
    private final LocalDateTime lastOccurredAt;
    private final AiAnalysisStatus aiAnalysisStatus;
    private final String aiSummary;
    private final String aiPossibleCause;
    private final String aiImpact;
    private final String aiSolution;
    private final List<String> aiCheckPoints;
    private final LocalDateTime analyzedAt;

    public AdminErrorIssueDetailResponseDto(Long issueId, String exceptionClass,
        String representativeMessage, ErrorSeverity severity, IssueStatus status, long occurrenceCount,
        LocalDateTime firstOccurredAt, LocalDateTime lastOccurredAt, AiAnalysisStatus aiAnalysisStatus,
        String aiSummary, String aiPossibleCause, String aiImpact, String aiSolution,
        List<String> aiCheckPoints, LocalDateTime analyzedAt) {
        this.issueId = issueId;
        this.exceptionClass = exceptionClass;
        this.representativeMessage = representativeMessage;
        this.severity = severity;
        this.status = status;
        this.occurrenceCount = occurrenceCount;
        this.firstOccurredAt = firstOccurredAt;
        this.lastOccurredAt = lastOccurredAt;
        this.aiAnalysisStatus = aiAnalysisStatus;
        this.aiSummary = aiSummary;
        this.aiPossibleCause = aiPossibleCause;
        this.aiImpact = aiImpact;
        this.aiSolution = aiSolution;
        this.aiCheckPoints = aiCheckPoints;
        this.analyzedAt = analyzedAt;
    }
}
