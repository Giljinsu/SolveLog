package com.study.blog.ai.thumbnail.controller;

import com.study.blog.service.CustomUserDetails;
import com.study.blog.ai.thumbnail.dto.ThumbnailJobCreateRequestDto;
import com.study.blog.ai.thumbnail.dto.ThumbnailJobResponseDto;
import com.study.blog.ai.thumbnail.service.ThumbnailJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/thumbnail-jobs")
@RequiredArgsConstructor
public class ThumbnailJobController {
    private final ThumbnailJobService thumbnailJobService;

    @PostMapping
    public ResponseEntity<ThumbnailJobResponseDto> createJob(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody ThumbnailJobCreateRequestDto requestDto) {
        ThumbnailJobResponseDto responseDto = thumbnailJobService.createJob(
            userDetails.getUserId(), userDetails.getUsername(), requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ThumbnailJobResponseDto> getJob(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable String jobId) {
        return ResponseEntity.ok(thumbnailJobService.getJob(userDetails.getUserId(), jobId));
    }
}
