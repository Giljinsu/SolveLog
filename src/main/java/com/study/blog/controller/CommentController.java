package com.study.blog.controller;

import com.study.blog.dto.comment.CommentRequestDto;
import com.study.blog.dto.comment.CommentResponseDto;
import com.study.blog.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/api/getComments/{postId}")
    public ResponseEntity<Result<List<CommentResponseDto>>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(Result.of(commentService.getComments(postId)));
    }

    @PostMapping("/api/createComment")
    public ResponseEntity<Void> createComment(@RequestBody CommentRequestDto requestDto) {
        commentService.createComment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/updateComment")
    public ResponseEntity<Void> updateComment(@RequestBody CommentRequestDto requestDto) {
        commentService.updateComment(requestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/deleteComment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
