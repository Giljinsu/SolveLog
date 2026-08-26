package com.study.blog.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.admin.dto.AdminErrorEventResponseDto;
import com.study.blog.admin.dto.AdminErrorIssueDetailResponseDto;
import com.study.blog.admin.dto.AdminErrorIssueResponseDto;
import com.study.blog.exception.MonitoringIssueNotFoundException;
import com.study.blog.ai.monitoring.domain.ErrorEvent;
import com.study.blog.ai.monitoring.domain.ErrorIssue;
import com.study.blog.ai.monitoring.repository.ErrorEventRepository;
import com.study.blog.ai.monitoring.repository.ErrorIssueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMonitoringService {

    private final ErrorIssueRepository errorIssueRepository;
    private final ErrorEventRepository errorEventRepository;
    private final ObjectMapper objectMapper;

    // 관리자 Monitoring 목록 - 최근 발생한 순
    public Page<AdminErrorIssueResponseDto> getIssues(Pageable pageable) {
        return errorIssueRepository.findAllByOrderByLastOccurredAtDesc(pageable)
            .map(issue -> new AdminErrorIssueResponseDto(
                issue.getId(),
                issue.getExceptionClass(),
                issue.getRepresentativeMessage(),
                issue.getSeverity(),
                issue.getStatus(),
                issue.getOccurrenceCount(),
                issue.getFirstOccurredAt(),
                issue.getLastOccurredAt(),
                issue.getAiAnalysisStatus()
            ));
    }

    // 관리자 Monitoring 상세 - Issue 정보 + AI 분석 결과
    public AdminErrorIssueDetailResponseDto getIssueDetail(Long issueId) {
        ErrorIssue issue = errorIssueRepository.findById(issueId)
            .orElseThrow(MonitoringIssueNotFoundException::new);

        return new AdminErrorIssueDetailResponseDto(
            issue.getId(),
            issue.getExceptionClass(),
            issue.getRepresentativeMessage(),
            issue.getSeverity(),
            issue.getStatus(),
            issue.getOccurrenceCount(),
            issue.getFirstOccurredAt(),
            issue.getLastOccurredAt(),
            issue.getAiAnalysisStatus(),
            issue.getAiSummary(),
            issue.getAiPossibleCause(),
            issue.getAiImpact(),
            issue.getAiSolution(),
            parseCheckPoints(issue.getAiCheckPoints()),
            issue.getAnalyzedAt()
        );
    }

    // 관리자 Monitoring 상세 - 최근 ErrorEvent 목록 (pagination)
    public Page<AdminErrorEventResponseDto> getEvents(Long issueId, Pageable pageable) {
        if (!errorIssueRepository.existsById(issueId)) {
            throw new MonitoringIssueNotFoundException();
        }

        return errorEventRepository.findByIssue_IdOrderByOccurredAtDesc(issueId, pageable)
            .map(this::toEventDto);
    }

    private AdminErrorEventResponseDto toEventDto(ErrorEvent event) {
        return new AdminErrorEventResponseDto(
            event.getId(),
            event.getOccurredAt(),
            event.getTraceId(),
            event.getRequestUri(),
            event.getHttpMethod(),
            event.getHttpStatus(),
            event.getUserId(),
            event.getUsername(),
            event.getIp(),
            event.getStackTraceExcerpt()
        );
    }

    private List<String> parseCheckPoints(String checkPointsJson) {
        if (checkPointsJson == null || checkPointsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(checkPointsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
