package com.study.blog.admin.dto;

import java.time.LocalDateTime;
import lombok.Getter;

// 관리자 Comments 목록 응답 - 대댓글 포함 전체 댓글을 평탄화해서 보여준다.
@Getter
public class AdminCommentResponseDto {
    private final Long commentId;
    private final String content;
    private final String username;
    private final String nickname;
    private final Long postId;
    private final String postTitle;
    private final LocalDateTime createdAt;
    private final Long parentCommentId;
    private final Boolean isReply;
    private final Boolean authorDeleted;

    public AdminCommentResponseDto(Long commentId, String content, String username, String nickname,
        Long postId, String postTitle, LocalDateTime createdAt, Long parentCommentId,
        Boolean authorDeleted) {
        this.commentId = commentId;
        this.content = content;
        this.username = username;
        this.nickname = nickname;
        this.postId = postId;
        this.postTitle = postTitle;
        this.createdAt = createdAt;
        this.parentCommentId = parentCommentId;
        this.isReply = parentCommentId != null;
        this.authorDeleted = authorDeleted;
    }
}
