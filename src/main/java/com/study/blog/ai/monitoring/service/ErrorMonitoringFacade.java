package com.study.blog.ai.monitoring.service;

import com.study.blog.ai.monitoring.classifier.ErrorAction;
import com.study.blog.ai.monitoring.classifier.ErrorClassifier;
import com.study.blog.ai.monitoring.dto.ErrorCaptureContext;
import com.study.blog.ai.monitoring.dto.ErrorCaptureResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * "분류 → 저장 → (신규 Issue면) AI 분석 트리거"를 한 곳에서 수행하는 진입점.
 * GlobalExceptionHandler, Batch 실패 처리 등 여러 호출부가 각자 이 순서를 반복 구현하면
 * analysis.md §12/이번 지시 §16의 "Monitoring 실패를 다시 Monitoring하지 않는다"는
 * 안전 규칙이 호출부마다 흩어져 유지보수 중 깨지기 쉽다. 이 메서드 밖으로는 어떤 예외도
 * 던지지 않는다 - 호출부는 이 호출 자체가 실패할 걱정 없이 fire-and-forget으로 쓰면 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorMonitoringFacade {

    private final ErrorClassifier errorClassifier;
    private final ErrorCaptureService errorCaptureService;
    private final ErrorAnalysisService errorAnalysisService;

    public void report(Throwable throwable, ErrorCaptureContext context) {
        try {
            ErrorAction action = errorClassifier.classify(throwable);
            if (action != ErrorAction.RECORD_AND_AI_ANALYZE) {
                return;
            }

            ErrorCaptureResult result = errorCaptureService.capture(throwable, context);
            if (result.newIssue()) {
                errorAnalysisService.analyze(result.issueId());
            }
        } catch (Exception captureFailure) {
            // 재귀 방지: 여기서 다시 report()/capture()를 호출하지 않는다. 로그만 남긴다.
            log.error("Error Monitoring 저장 자체가 실패함", captureFailure);
        }
    }
}
