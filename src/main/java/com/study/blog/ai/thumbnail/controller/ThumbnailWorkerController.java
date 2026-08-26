package com.study.blog.ai.thumbnail.controller;

import com.study.blog.ai.thumbnail.dto.ThumbnailJobClaimResponseDto;
import com.study.blog.ai.thumbnail.dto.ThumbnailJobFailRequestDto;
import com.study.blog.ai.thumbnail.service.ThumbnailJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Mac Worker 전용 API - Worker Token 인증으로 사용자 JWT 인증과 분리된다.
@RestController
@RequestMapping("/api/internal/thumbnail-jobs")
@RequiredArgsConstructor
public class ThumbnailWorkerController {
    private final ThumbnailJobService thumbnailJobService;

    @PostMapping("/claim")
    public ResponseEntity<ThumbnailJobClaimResponseDto> claim() {
        return thumbnailJobService.claimJob()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping(value = "/{jobId}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> complete(
        @PathVariable String jobId,
        @RequestParam("image") MultipartFile image) {
        thumbnailJobService.completeJob(jobId, image);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{jobId}/fail")
    public ResponseEntity<Void> fail(
        @PathVariable String jobId,
        @Valid @RequestBody ThumbnailJobFailRequestDto requestDto) {
        thumbnailJobService.failJob(jobId, requestDto.getMessage());
        return ResponseEntity.ok().build();
    }
}
