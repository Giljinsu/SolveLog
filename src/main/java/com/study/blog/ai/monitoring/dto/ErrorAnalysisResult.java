package com.study.blog.ai.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// GPT가 error-analysis-system-prompt.txt 지시에 따라 반환하는 JSON 구조(analysis.md §10, §14).
@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorAnalysisResult(
    String summary,
    String possibleCause,
    String impact,
    String solution,
    List<String> checkPoints,
    String severity
) {
}
