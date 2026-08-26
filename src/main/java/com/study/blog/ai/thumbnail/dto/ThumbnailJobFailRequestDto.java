package com.study.blog.ai.thumbnail.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ThumbnailJobFailRequestDto {

    @NotBlank
    private String message;
}
