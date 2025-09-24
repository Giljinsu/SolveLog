package com.study.blog.dto.comment;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommentRequestDto {
    private Long commentId;
    private Long userId;
    private Long postId;
    private Long parentCommentId;
    private String username;
    private String comment;
    private Long mentionCommentId;
    private String mentionNickname;

    public CommentRequestDto(Long userId, Long postId, String comment) {
        this.userId = userId;
        this.postId = postId;
        this.comment = comment;
    }

}
