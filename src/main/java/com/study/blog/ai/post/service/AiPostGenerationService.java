package com.study.blog.ai.post.service;

import com.study.blog.client.OpenAiClient;
import com.study.blog.ai.post.dto.AiPostGenerateRequestDto;
import com.study.blog.ai.post.dto.AiPostGenerateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 문제 설명 + 사용자 코드를 기반으로 GPT에게 전달할 프롬프트를 구성하고,
 * OpenAiClient를 호출해 Markdown 초안을 생성한다.
 * 게시글 저장(PostService), 파일 처리(FileService)와는 연결하지 않는다.
 */
@Service
@RequiredArgsConstructor
public class AiPostGenerationService {

    private final OpenAiClient openAiClient;

    public AiPostGenerateResponseDto generatePostDraft(AiPostGenerateRequestDto requestDto) {
        String userPrompt = buildUserPrompt(requestDto);

        String markdown = openAiClient.generatePostDraft(userPrompt);

        return new AiPostGenerateResponseDto(markdown);
    }

    private String buildUserPrompt(AiPostGenerateRequestDto requestDto) {
        return "## 문제 설명\n\n"
            + requestDto.getProblemContent()
            + "\n\n## 사용자 코드\n\n"
            + requestDto.getSolutionCode();
    }
}