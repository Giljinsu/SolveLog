package com.study.blog.ai.thumbnail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ThumbnailJobCreateRequestDto {

    @Size(max = 100)
    private String problemTitle;

    @NotBlank
    @Size(max = 5000)
    private String problemDescription;
}
