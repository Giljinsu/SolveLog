package com.study.blog.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.study.blog.exception.AiGenerationException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * OpenAI Chat Completions API와의 HTTP 통신만 담당한다.
 * 요청/응답 JSON 구조를 이 클래스 밖으로 노출하지 않는다.
 */
@Slf4j
@Component
public class OpenAiClient {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String POST_DRAFT_SYSTEM_PROMPT_PATH = "prompts/post-draft-system-prompt.txt";
    private static final String THUMBNAIL_PROMPT_SYSTEM_PROMPT_PATH = "prompts/thumbnail-prompt-system-prompt.txt";
    private static final String ERROR_ANALYSIS_SYSTEM_PROMPT_PATH = "prompts/error-analysis-system-prompt.txt";

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;

    private String postDraftSystemPrompt;
    private String thumbnailPromptSystemPrompt;
    private String errorAnalysisSystemPrompt;

    public OpenAiClient(
        @Value("${openai.api-key}") String apiKey,
        @Value("${openai.model}") String model,
        @Value("${openai.connect-timeout-ms:5000}") long connectTimeoutMillis,
        @Value("${openai.read-timeout-ms:90000}") long readTimeoutMillis
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restTemplate = new RestTemplateBuilder()
            .connectTimeout(java.time.Duration.ofMillis(connectTimeoutMillis))
            .readTimeout(java.time.Duration.ofMillis(readTimeoutMillis))
            .build();
    }

    @PostConstruct
    public void init() {
        this.postDraftSystemPrompt = loadSystemPrompt(POST_DRAFT_SYSTEM_PROMPT_PATH);
        this.thumbnailPromptSystemPrompt = loadSystemPrompt(THUMBNAIL_PROMPT_SYSTEM_PROMPT_PATH);
        this.errorAnalysisSystemPrompt = loadSystemPrompt(ERROR_ANALYSIS_SYSTEM_PROMPT_PATH);
    }

    private String loadSystemPrompt(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("system prompt 파일을 불러오지 못했습니다: " + path, e);
        }
    }

    // 문제풀이 Markdown 초안 생성
    public String generatePostDraft(String userPrompt) {
        return chatComplete(postDraftSystemPrompt, userPrompt);
    }

    // 썸네일 이미지 생성용 Prompt 생성
    public String generateThumbnailPrompt(String userPrompt) {
        return chatComplete(thumbnailPromptSystemPrompt, userPrompt);
    }

    // Error Monitoring - 서버 오류 분석 (JSON 문자열을 그대로 반환, 파싱은 호출부 책임)
    public String analyzeError(String userPrompt) {
        return chatComplete(errorAnalysisSystemPrompt, userPrompt);
    }

    // OpenAI Chat Completions 공통 호출 로직
    private String chatComplete(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            )
        );

        HttpEntity<Map<String, Object>> httpEntity =
            new HttpEntity<>(requestBody, headers);

        ChatCompletionResponse response;
        try {
            response = restTemplate.postForObject(CHAT_COMPLETIONS_URL, httpEntity, ChatCompletionResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("OpenAI API 인증 실패 (API Key 확인 필요)", e);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.error("OpenAI API rate limit 초과", e);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (HttpClientErrorException e) {
            log.error("OpenAI API 요청 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (HttpServerErrorException e) {
            log.error("OpenAI API 서버 오류: status={}", e.getStatusCode(), e);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (ResourceAccessException e) {
            log.error("OpenAI API 호출 timeout 또는 네트워크 오류", e);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (RestClientException e) {
            log.error("OpenAI API 호출 중 알 수 없는 오류", e);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.", e);
        }

        String content = extractContent(response);
        if (content == null || content.isBlank()) {
            log.error("OpenAI 응답에 content가 없음: {}", response);
            throw new AiGenerationException("AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }

        return content.trim();
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return null;
        }
        ChatCompletionResponse.Message message = response.choices().get(0).message();
        return message != null ? message.content() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Choice(Message message) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Message(String role, String content) {
        }
    }
}