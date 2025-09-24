package com.study.blog.controller;

import com.study.blog.dto.postTag.PostTagResponseDto;
import com.study.blog.service.PostTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostTagController {
    private final PostTagService postTagService;

    @GetMapping("/api/getPostCountPerTagByUsername/{username}")
    public ResponseEntity<PostTagResponseDto> getPostCountPerTagByUsername(@PathVariable String username) {
        return ResponseEntity.ok(postTagService.getPostCountPerTagByUsername(username));
    }

    @GetMapping("/api/getLikePostCountPerTagByUsername/{username}")
    public ResponseEntity<PostTagResponseDto> getLikePostCountPerTagByUsername(@PathVariable String username) {
        return ResponseEntity.ok(postTagService.getLikePostCountPerTagByUsername(username));
    }
}
