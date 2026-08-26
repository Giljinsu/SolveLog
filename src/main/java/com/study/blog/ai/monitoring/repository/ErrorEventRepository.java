package com.study.blog.ai.monitoring.repository;

import com.study.blog.ai.monitoring.domain.ErrorEvent;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ErrorEventRepository extends JpaRepository<ErrorEvent, Long> {

    // Admin 상세 화면의 최근 ErrorEvent 목록 (pagination)
    Page<ErrorEvent> findByIssue_IdOrderByOccurredAtDesc(Long issueId, Pageable pageable);

    // ErrorEvent 보존 기간 정리 배치(§5, §24) - ErrorIssue는 삭제하지 않고 Event만 정리한다.
    @Modifying
    @Query("delete from ErrorEvent e where e.occurredAt < :cutoff")
    int deleteByOccurredAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
