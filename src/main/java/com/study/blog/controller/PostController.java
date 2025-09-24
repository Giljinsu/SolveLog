package com.study.blog.controller;

import com.study.blog.dto.post.PostRequestDto;
import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.dto.post.PostSliceResponseDto;
import com.study.blog.dto.post.SearchCondition;
import com.study.blog.service.PostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/api/getPostList")
    public PostSliceResponseDto getPostList(@ModelAttribute SearchCondition searchCondition, Pageable pageable) {
        return postService.getList(searchCondition, pageable);
    }

    @GetMapping("/api/getPostDetail/{postId}")
    public Result<PostResponseDto> getPostDetail(@PathVariable Long postId) {
        return Result.single(postService.getDetailPost(postId));
    }

    @GetMapping("/api/getTmpPostList/{username}")
    public PostSliceResponseDto getTmpPostList(@PathVariable String username, Pageable pageable) {
        return postService.getTmpPostList(username, pageable);
    }

    @GetMapping("/api/getPostByTagIdAndUsername")
    public PostSliceResponseDto getPostByTagIdAndUsername(@ModelAttribute SearchCondition searchCondition, Pageable pageable) {
        return postService.getPostByTagIdAndUsername(searchCondition, pageable);
    }

    @GetMapping("/api/getLikesPostByUser")
    public PostSliceResponseDto getLikesPostByUser (@ModelAttribute SearchCondition searchCondition, Pageable pageable) {
        return postService.getLikePostList(searchCondition, pageable);
    }

    @PostMapping("/api/posts/{postId}/view")
    public ResponseEntity<Map<String, Integer>> increaseViewCount(
        @PathVariable Long postId,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        String cookieName = "viewed_"+postId;

        Map<String, Integer> response = new HashMap<>();


        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    response.put("viewCount", postService.getViews(postId));
                    return ResponseEntity.ok(response);
                }
            }
        }

        Cookie newCookie = new Cookie(cookieName, "true");
        newCookie.setMaxAge(60*60*24); // 1일
        newCookie.setPath("/");
        httpServletResponse.addCookie(newCookie);

        int newViews = postService.addView(postId);
        response.put("viewCount",newViews);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/createPost")
    public ResponseEntity<Long> createPost(@RequestBody PostRequestDto postRequestDto) {
        Long postId = postService.createPost(postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postId);
    }

    @PostMapping("/api/deletePost/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/updatePost")
    public ResponseEntity<Void> updatePost(@RequestBody PostRequestDto postRequestDto) {
        postService.updatePost(postRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
