package com.study.blog.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagResponseDto {
    private Long tagId;
    private Long postId;
    private String tagName;
    private int postCnt;

    public TagResponseDto(Long tagId, String tagName) {
        this.tagId = tagId;
        this.tagName = tagName;
    }

    public TagResponseDto(Long tagId, Long postId, String tagName ) {
        this.tagId = tagId;
        this.postId = postId;
        this.tagName = tagName;
    }

    public TagResponseDto(Long tagId, String tagName, int postCnt) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.postCnt = postCnt;
    }
}
