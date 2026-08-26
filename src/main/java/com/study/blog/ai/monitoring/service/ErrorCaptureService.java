package com.study.blog.ai.monitoring.service;

import com.study.blog.ai.monitoring.domain.ErrorEvent;
import com.study.blog.ai.monitoring.domain.ErrorIssue;
import com.study.blog.ai.monitoring.domain.ErrorSeverity;
import com.study.blog.ai.monitoring.dto.ErrorCaptureContext;
import com.study.blog.ai.monitoring.dto.ErrorCaptureResult;
import com.study.blog.ai.monitoring.fingerprint.ErrorFingerprintGenerator;
import com.study.blog.ai.monitoring.repository.ErrorEventRepository;
import com.study.blog.ai.monitoring.repository.ErrorIssueRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 저장 대상으로 분류된 예외를 ErrorIssue/ErrorEvent로 저장한다(analysis.md §9, §12).
 * 원본 요청의 Transaction이 rollback되더라도 이 저장은 살아남아야 하므로 REQUIRES_NEW를 쓴다
 * (FileService.java의 기존 REQUIRES_NEW 사용 선례를 따른다).
 *
 * 이 클래스 자체가 실패하는 경우(DB 장애 등)의 처리는 호출부의 책임이다 - 호출부는 반드시
 * try/catch로 감싸고 실패 시 log.error만 남긴다. 이 클래스 안에서 스스로를 재호출하지 않는다.
 */
@Service
@RequiredArgsConstructor
public class ErrorCaptureService {

    private static final int MAX_STACK_FRAMES = 20;
    private static final int MAX_MESSAGE_LENGTH = 1000;

    private final ErrorIssueRepository errorIssueRepository;
    private final ErrorEventRepository errorEventRepository;
    private final ErrorFingerprintGenerator fingerprintGenerator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ErrorCaptureResult capture(Throwable throwable, ErrorCaptureContext context) {
        LocalDateTime now = LocalDateTime.now();
        String fingerprint = fingerprintGenerator.generate(throwable);
        String message = truncate(throwable.getMessage(), MAX_MESSAGE_LENGTH);

        ErrorIssue issue = errorIssueRepository.findByFingerprint(fingerprint).orElse(null);
        boolean newIssue = issue == null;
        if (newIssue) {
            issue = errorIssueRepository.save(ErrorIssue.createNew(
                fingerprint, throwable.getClass().getName(), message, ErrorSeverity.MEDIUM, now));
        } else {
            // occurrence_count/last_occurred_at 두 컬럼만 UPDATE한다 - 아래 issue 참조는
            // ErrorEvent의 FK를 채우는 용도로만 쓰고 그 상태를 다시 save()하지 않는다
            // (ErrorIssueRepository.incrementOccurrence의 lost update 방지 주석 참고).
            errorIssueRepository.incrementOccurrence(issue.getId(), now);
        }

        ErrorEvent event = ErrorEvent.create(
            issue,
            now,
            context.traceId(),
            context.requestUri(),
            context.httpMethod(),
            context.httpStatus(),
            context.userId(),
            context.username(),
            context.ip(),
            buildStackTraceExcerpt(throwable)
        );
        errorEventRepository.save(event);

        return new ErrorCaptureResult(issue.getId(), newIssue);
    }

    private String buildStackTraceExcerpt(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString());

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace != null) {
            int limit = Math.min(stackTrace.length, MAX_STACK_FRAMES);
            for (int i = 0; i < limit; i++) {
                sb.append("\n\tat ").append(stackTrace[i]);
            }
            if (stackTrace.length > limit) {
                sb.append("\n\t... ").append(stackTrace.length - limit).append(" more");
            }
        }
        return sb.toString();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
