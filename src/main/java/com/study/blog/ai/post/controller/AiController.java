package com.study.blog.ai.post.controller;

import com.study.blog.ai.post.dto.AiPostGenerateRequestDto;
import com.study.blog.ai.post.dto.AiPostGenerateResponseDto;
import com.study.blog.ai.post.service.AiPostGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiPostGenerationService aiPostGenerationService;

    @PostMapping("/generatePostDraft")
    public ResponseEntity<AiPostGenerateResponseDto> generatePostDraft(
        @Valid @RequestBody AiPostGenerateRequestDto requestDto) {
        return ResponseEntity.ok(aiPostGenerationService.generatePostDraft(requestDto));
    }
}