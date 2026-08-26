package com.study.blog.admin.dto;

import java.time.LocalDateTime;
import lombok.Getter;

// 관리자 Posts 목록 응답 - 임시글 포함, category 등 목록에 불필요한 필드는 조회하지 않는다.
@Getter
public class AdminPostResponseDto {
    private final Long postId;
    private final String title;
    private final String username;
    private final String nickname;
    private final Boolean isTemp;
    private final LocalDateTime createdAt;
    private final int viewCount;
    private final Boolean authorDeleted;

    public AdminPostResponseDto(Long postId, String title, String username, String nickname,
        Boolean isTemp, LocalDateTime createdAt, int viewCount, Boolean authorDeleted) {
        this.postId = postId;
        this.title = title;
        this.username = username;
        this.nickname = nickname;
        this.isTemp = isTemp;
        this.createdAt = createdAt;
        this.viewCount = viewCount;
        this.authorDeleted = authorDeleted;
    }
}
