package com.study.blog.admin.controller;

import com.study.blog.admin.dto.AdminCommentResponseDto;
import com.study.blog.admin.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 - SecurityConfig에서 /api/admin/** 는 ADMIN만 접근 가능
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping
    public ResponseEntity<Page<AdminCommentResponseDto>> getComments(
        @RequestParam(required = false) String content,
        @RequestParam(required = false) String author,
        Pageable pageable) {
        return ResponseEntity.ok(adminCommentService.getComments(content, author, pageable));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        adminCommentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
