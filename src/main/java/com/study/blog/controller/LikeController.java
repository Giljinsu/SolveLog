package com.study.blog.controller;

import com.study.blog.dto.likes.LikesRequestDto;
import com.study.blog.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @GetMapping("/api/isLiked")
    public ResponseEntity<Boolean> isLiked(@ModelAttribute LikesRequestDto likesRequestDto) {
        return ResponseEntity.ok(likeService.isLiked(likesRequestDto));
    }

    @GetMapping("/api/getLikesCount/{postId}")
    public ResponseEntity<Integer> getLikesCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikesCount(postId));
    }

    @PostMapping("/api/createLike")
    public ResponseEntity<Void> createLike(@RequestBody LikesRequestDto requestDto) {
        likeService.createLike(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/deleteLike")
    public ResponseEntity<Void> deleteLike(@RequestBody LikesRequestDto requestDto) {
        likeService.deleteLike(requestDto);
        return ResponseEntity.noContent().build();
    }
}
