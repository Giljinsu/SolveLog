package com.study.blog.dto.likes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class LikesRequestDto {
    private Long userId;
    private String username;
    private Long postId;

    public LikesRequestDto(String username, Long postId) {
        this.username = username;
        this.postId = postId;
    }

    public LikesRequestDto(Long userId, String username, Long postId) {
        this.userId = userId;
        this.username = username;
        this.postId = postId;
    }
}
