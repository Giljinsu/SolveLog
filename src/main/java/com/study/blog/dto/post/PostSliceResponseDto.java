package com.study.blog.dto.post;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class PostSliceResponseDto {
    private List<PostResponseDto> content;
    private Boolean hasNext;

    public PostSliceResponseDto(List<PostResponseDto> content, Boolean hasNext) {
        this.content = content;
        this.hasNext = hasNext;
    }
}
