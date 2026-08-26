package com.study.blog.ai.thumbnail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ThumbnailJobClaimResponseDto {
    private String jobId;
    private String prompt;
}
