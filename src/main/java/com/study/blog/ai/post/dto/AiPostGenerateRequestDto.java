package com.study.blog.ai.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class AiPostGenerateRequestDto {

    @NotBlank
    @Size(max = 5000)
    private String problemContent;

    @NotBlank
    @Size(max = 10000)
    private String solutionCode;
}