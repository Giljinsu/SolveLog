package com.study.blog.dto.comment;

import com.study.blog.dto.file.FileResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class CommentResponseDto {
    private Long commentId;
    private Long parentCommentId;
    private String nickname;
    private String username;
    private String comment;
    private Integer commentCnt;
    private LocalDateTime createdDate;
    private List<CommentResponseDto> childComments;
    private FileResponseDto userImg;

    public CommentResponseDto(Long commentId, String nickname, String username, String comment,
        LocalDateTime createdDate, Long parentCommentId, FileResponseDto userImg) {
        this.commentId = commentId;
        this.nickname = nickname;
        this.username = username;
        this.comment = comment;
        this.createdDate = createdDate;
        this.parentCommentId = parentCommentId;
        this.userImg = userImg;
    }

    public CommentResponseDto(Long commentId, String nickname, String username, String comment,
        LocalDateTime createdDate, Long parentCommentId, List<CommentResponseDto> childComments,
        FileResponseDto userImg,
        Integer commentCnt) {
        this.commentId = commentId;
        this.nickname = nickname;
        this.username = username;
        this.comment = comment;
        this.createdDate = createdDate;
        this.parentCommentId = parentCommentId;
        this.childComments = childComments;
        this.userImg = userImg;
        this.commentCnt = commentCnt;
    }

    public CommentResponseDto(Long commentId, String nickname, String username, String comment,
        LocalDateTime createdDate, Long parentCommentId, List<CommentResponseDto> childComments,
        FileResponseDto userImg) {
        this.commentId = commentId;
        this.nickname = nickname;
        this.username = username;
        this.comment = comment;
        this.createdDate = createdDate;
        this.parentCommentId = parentCommentId;
        this.childComments = childComments;
        this.userImg = userImg;
    }
}
