package com.study.blog.ai.monitoring.domain;

import com.study.blog.entity.basicentity.BasicDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 동일 fingerprint의 예외를 그룹화한 대표 레코드. 삭제하지 않고 계속 보존한다(§5, §9 정책).
@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class ErrorIssue extends BasicDate {

    @Id @GeneratedValue
    @Column(name = "issue_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String fingerprint;

    @Column(name = "exception_class", nullable = false)
    private String exceptionClass;

    @Column(name = "representative_message", length = 1000)
    private String representativeMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ErrorSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status;

    @Column(name = "first_occurred_at", nullable = false)
    private LocalDateTime firstOccurredAt;

    @Column(name = "last_occurred_at", nullable = false)
    private LocalDateTime lastOccurredAt;

    @Column(name = "occurrence_count", nullable = false)
    private long occurrenceCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_analysis_status", nullable = false, length = 20)
    private AiAnalysisStatus aiAnalysisStatus;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "ai_possible_cause", columnDefinition = "TEXT")
    private String aiPossibleCause;

    @Column(name = "ai_impact", columnDefinition = "TEXT")
    private String aiImpact;

    @Column(name = "ai_solution", columnDefinition = "TEXT")
    private String aiSolution;

    @Column(name = "ai_check_points", columnDefinition = "TEXT")
    private String aiCheckPoints;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    public static ErrorIssue createNew(String fingerprint, String exceptionClass, String message,
        ErrorSeverity severity, LocalDateTime occurredAt) {
        ErrorIssue issue = new ErrorIssue();
        issue.setFingerprint(fingerprint);
        issue.setExceptionClass(exceptionClass);
        issue.setRepresentativeMessage(message);
        issue.setSeverity(severity);
        issue.setStatus(IssueStatus.OPEN);
        issue.setFirstOccurredAt(occurredAt);
        issue.setLastOccurredAt(occurredAt);
        issue.setOccurrenceCount(1);
        issue.setAiAnalysisStatus(AiAnalysisStatus.PENDING);
        return issue;
    }

    // 재발생 기록/AI 분석 결과 반영은 이 엔티티의 메서드로 하지 않는다. ErrorCaptureService와
    // ErrorAnalysisService가 동시에 같은 row를 각자 로드한 뒤 전체 엔티티를 save()하면, 나중에
    // 커밋되는 쪽이 자신이 읽은 시점의 stale한 값으로 다른 쪽이 쓴 컬럼을 덮어써버리는
    // lost update가 발생한다(실제로 재현됨). 그래서 두 갱신 모두
    // ErrorIssueRepository.incrementOccurrence()/applyAnalysisResult()/markAnalysisFailed()의
    // 컬럼 단위 UPDATE 쿼리로만 수행한다.
}
