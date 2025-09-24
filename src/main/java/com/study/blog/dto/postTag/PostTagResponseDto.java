package com.study.blog.dto.postTag;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class PostTagResponseDto {
    private List<TagCountDto> tagCountDtos;
    private Long totalCount; // 게시글 전체 수
}
