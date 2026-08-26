package com.study.blog.ai.monitoring.scheduler;

import com.study.blog.ai.monitoring.repository.ErrorEventRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ErrorEvent 보존 기간 정리 배치(analysis.md §5/§8, 이번 지시 §5/§24).
 * ErrorIssue는 삭제하지 않는다 - occurrence_count는 누적 발생 횟수이므로 Event 삭제와 무관하게
 * 유지된다. 기존 통계 Batch처럼 별도 Spring Batch Job/Step으로 만들 만큼의 복잡도가 필요
 * 없어(단순 기간 기반 DELETE 한 번) StatisticBatchScheduler와 동일한 @Scheduled 패턴만
 * 재사용하고, Spring Batch 프레임워크는 도입하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorEventCleanupScheduler {

    private final ErrorEventRepository errorEventRepository;

    @Value("${monitoring.error-event-retention-days:90}")
    private int retentionDays;

    // 통계 배치(자정)와 겹치지 않도록 30분 뒤에 실행한다.
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        try {
            int deletedCount = errorEventRepository.deleteByOccurredAtBefore(cutoff);
            log.info("ErrorEvent 정리 배치 완료 - retentionDays={}, cutoff={}, 삭제 건수={}",
                retentionDays, cutoff, deletedCount);
        } catch (Exception e) {
            log.error("ErrorEvent 정리 배치 실패 - retentionDays={}, cutoff={}", retentionDays, cutoff, e);
        }
    }
}
