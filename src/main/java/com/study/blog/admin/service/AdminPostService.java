package com.study.blog.admin.service;

import com.study.blog.admin.dto.AdminPostResponseDto;
import com.study.blog.repository.PostRepository;
import com.study.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPostService {

    private final PostRepository postRepository;
    private final PostService postService;

    // 관리자 Posts 목록 - 임시글 포함, 제목/작성자 개별 검색
    public Page<AdminPostResponseDto> getPosts(String title, String author, Pageable pageable) {
        return postRepository.findPostsForAdmin(title, author, pageable);
    }

    // 관리자 Post 삭제 - 작성자 검사 없이 삭제, /api/admin/** 보호를 최종 권한으로 신뢰한다
    @Transactional
    public void deletePost(Long postId) {
        postService.deletePostByAdmin(postId);
    }
}
