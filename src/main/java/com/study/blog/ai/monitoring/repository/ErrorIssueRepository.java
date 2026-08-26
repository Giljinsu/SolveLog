package com.study.blog.ai.monitoring.repository;

import com.study.blog.ai.monitoring.domain.AiAnalysisStatus;
import com.study.blog.ai.monitoring.domain.ErrorIssue;
import com.study.blog.ai.monitoring.domain.ErrorSeverity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ErrorIssueRepository extends JpaRepository<ErrorIssue, Long> {

    Optional<ErrorIssue> findByFingerprint(String fingerprint);

    // 관리자 목록 - 최근 발생한 순. 별도 검색 조건이 없어 QueryDSL 없이 파생 쿼리로 충분하다.
    Page<ErrorIssue> findAllByOrderByLastOccurredAtDesc(Pageable pageable);

    // 재발생 기록 - occurrence_count/last_occurred_at 두 컬럼만 UPDATE한다.
    // ErrorAnalysisService(@Async)가 같은 Issue를 로드해 전체 엔티티를 save()하면, 그 트랜잭션이
    // 나중에 커밋될 때 자신이 읽은 시점의(더 오래된) occurrence_count로 이 값을 덮어써버리는
    // lost update가 발생할 수 있다. 두 쓰기 경로 모두 필요한 컬럼만 UPDATE하도록 분리해
    // 이 문제를 구조적으로 없앤다.
    @Modifying
    @Query("update ErrorIssue i set i.occurrenceCount = i.occurrenceCount + 1, i.lastOccurredAt = :occurredAt "
        + "where i.id = :issueId")
    void incrementOccurrence(@Param("issueId") Long issueId, @Param("occurredAt") LocalDateTime occurredAt);

    // AI 분석 성공 - AI 관련 컬럼만 UPDATE한다. severity는 GPT가 값을 주지 않으면 기존 값을 유지한다.
    @Modifying
    @Query("update ErrorIssue i set i.aiAnalysisStatus = :status, i.aiSummary = :summary, "
        + "i.aiPossibleCause = :possibleCause, i.aiImpact = :impact, i.aiSolution = :solution, "
        + "i.aiCheckPoints = :checkPoints, i.severity = coalesce(:severity, i.severity), "
        + "i.analyzedAt = :analyzedAt "
        + "where i.id = :issueId")
    void applyAnalysisResult(@Param("issueId") Long issueId, @Param("status") AiAnalysisStatus status,
        @Param("summary") String summary, @Param("possibleCause") String possibleCause,
        @Param("impact") String impact, @Param("solution") String solution,
        @Param("checkPoints") String checkPoints, @Param("severity") ErrorSeverity severity,
        @Param("analyzedAt") LocalDateTime analyzedAt);

    // AI 분석 실패 - 상태/시각만 UPDATE하고 기존 AI 필드는 건드리지 않는다.
    @Modifying
    @Query("update ErrorIssue i set i.aiAnalysisStatus = :status, i.analyzedAt = :analyzedAt "
        + "where i.id = :issueId")
    void markAnalysisFailed(@Param("issueId") Long issueId, @Param("status") AiAnalysisStatus status,
        @Param("analyzedAt") LocalDateTime analyzedAt);
}
