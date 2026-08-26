package com.study.blog.ai.monitoring.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.client.OpenAiClient;
import com.study.blog.ai.monitoring.domain.AiAnalysisStatus;
import com.study.blog.ai.monitoring.domain.ErrorEvent;
import com.study.blog.ai.monitoring.domain.ErrorIssue;
import com.study.blog.ai.monitoring.domain.ErrorSeverity;
import com.study.blog.ai.monitoring.dto.ErrorAnalysisResult;
import com.study.blog.ai.monitoring.repository.ErrorEventRepository;
import com.study.blog.ai.monitoring.repository.ErrorIssueRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신규 ErrorIssue에 대한 GPT 분석을 수행한다(analysis.md §10, §11, §16 순환 방지).
 *
 * 반드시 지켜야 하는 규칙 (이번 지시 §16과 동일):
 * - 이 메서드 안에서 발생하는 어떤 예외(AiGenerationException, JSON 파싱 실패 등)도
 *   밖으로 던지지 않는다. 실패 시 ai_analysis_status = FAILED만 기록하고 종료한다.
 * - ErrorCaptureService/ErrorClassifier를 다시 호출하지 않는다. 이 메서드가 만드는
 *   실패는 그 자체로 Monitoring 파이프라인의 "종착점"이다.
 * - 사용자 응답 Thread와 완전히 분리하기 위해 @Async로 실행한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorAnalysisService {

    private final ErrorIssueRepository errorIssueRepository;
    private final ErrorEventRepository errorEventRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    // @Async와 @Transactional을 같은 메서드에 직접 붙인다 - self-invocation으로 두 메서드를
    // 나누면(analyze()가 this.doAnalyze()를 호출) @Transactional 프록시가 적용되지 않는
    // Spring AOP의 잘 알려진 함정에 걸리기 때문이다. 메서드 전체를 try/catch로 감싸 어떤
    // 예외도 밖으로 던지지 않는다(순환 방지 원칙).
    @Async("errorAnalysisExecutor")
    @Transactional
    public void analyze(Long issueId) {
        try {
            doAnalyze(issueId);
        } catch (Exception e) {
            log.error("Error Monitoring AI 분석 중 처리되지 않은 예외 발생 (issueId={})", issueId, e);
        }
    }

    private void doAnalyze(Long issueId) {
        ErrorIssue issue = errorIssueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            log.warn("AI 분석 대상 ErrorIssue를 찾을 수 없음 (issueId={})", issueId);
            return;
        }
        if (issue.getAiAnalysisStatus() != AiAnalysisStatus.PENDING) {
            // 이미 분석 완료/실패한 Issue는 재분석하지 않는다 (analysis.md §9, 이번 지시 §12).
            return;
        }

        String userPrompt = buildUserPrompt(issue);

        String rawResponse;
        try {
            rawResponse = openAiClient.analyzeError(userPrompt);
        } catch (Exception e) {
            log.error("Error Monitoring AI 분석 GPT 호출 실패 (issueId={})", issueId, e);
            errorIssueRepository.markAnalysisFailed(issueId, AiAnalysisStatus.FAILED, LocalDateTime.now());
            return;
        }

        ErrorAnalysisResult result;
        try {
            result = objectMapper.readValue(rawResponse, ErrorAnalysisResult.class);
        } catch (Exception e) {
            log.error("Error Monitoring AI 응답 파싱 실패 (issueId={}): {}", issueId, rawResponse, e);
            errorIssueRepository.markAnalysisFailed(issueId, AiAnalysisStatus.FAILED, LocalDateTime.now());
            return;
        }

        String checkPointsJson = serializeCheckPoints(result.checkPoints());
        ErrorSeverity severity = parseSeverity(result.severity());

        // occurrence_count 등 다른 컬럼은 건드리지 않고 AI 관련 컬럼만 UPDATE한다 - 동시에
        // 들어올 수 있는 ErrorCaptureService.incrementOccurrence()와의 lost update를 피하기
        // 위함이다(ErrorIssueRepository 주석 참고).
        errorIssueRepository.applyAnalysisResult(
            issueId,
            AiAnalysisStatus.DONE,
            result.summary(),
            result.possibleCause(),
            result.impact(),
            result.solution(),
            checkPointsJson,
            severity,
            LocalDateTime.now()
        );
    }

    private String buildUserPrompt(ErrorIssue issue) {
        ErrorEvent latestEvent = errorEventRepository
            .findByIssue_IdOrderByOccurredAtDesc(issue.getId(), PageRequest.of(0, 1, Sort.unsorted()))
            .stream()
            .findFirst()
            .orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append("## exceptionClass\n").append(issue.getExceptionClass()).append("\n\n");
        sb.append("## message\n").append(nullToEmpty(issue.getRepresentativeMessage())).append("\n\n");
        sb.append("## occurrenceCount\n").append(issue.getOccurrenceCount()).append("\n\n");

        if (latestEvent != null) {
            sb.append("## requestUri\n").append(nullToEmpty(latestEvent.getRequestUri())).append("\n\n");
            sb.append("## httpMethod\n").append(nullToEmpty(latestEvent.getHttpMethod())).append("\n\n");
            sb.append("## stackTraceExcerpt\n").append(nullToEmpty(latestEvent.getStackTraceExcerpt()));
        }

        return sb.toString();
    }

    private String serializeCheckPoints(List<String> checkPoints) {
        if (checkPoints == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(checkPoints);
        } catch (Exception e) {
            return null;
        }
    }

    private ErrorSeverity parseSeverity(String severity) {
        if (severity == null) {
            return null;
        }
        try {
            return ErrorSeverity.valueOf(severity.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
