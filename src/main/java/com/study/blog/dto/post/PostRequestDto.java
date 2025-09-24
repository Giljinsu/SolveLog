package com.study.blog.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class PostRequestDto {
    private Long postId;
    private Long userId;
    private String username;
    private Long categoryId;
    private String categoryType;
    private String title;
    private String content;
    private String tags;
    private String summary;
    private Boolean isTemp;

    public PostRequestDto(Long userId, Long categoryId, String title, String content,
        String tags, String summary, Boolean isTemp) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.summary = summary;
        this.isTemp = isTemp;
    }
}
