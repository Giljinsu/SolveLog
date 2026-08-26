package com.study.blog.admin.controller;

import com.study.blog.admin.dto.AdminPostResponseDto;
import com.study.blog.admin.service.AdminPostService;
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
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    public ResponseEntity<Page<AdminPostResponseDto>> getPosts(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String author,
        Pageable pageable) {
        return ResponseEntity.ok(adminPostService.getPosts(title, author, pageable));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}
