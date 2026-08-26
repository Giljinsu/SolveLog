package com.study.blog.repository;

import com.study.blog.admin.dto.AdminPostResponseDto;
import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.dto.post.SearchCondition;
import com.study.blog.entity.Post;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostRepositoryCustom {

    Slice<PostResponseDto> findListWithSearchCondition(SearchCondition searchCondition, Pageable pageable);
    Page<PostResponseDto> findListWithSearchCondition_Page(SearchCondition searchCondition, Pageable pageable);
    PostResponseDto findDetailPostById(Long postId);
    Slice<PostResponseDto> findByUsernameAndIsTemp(String username, Pageable pageable);
    Slice<PostResponseDto> getPostByTagIdAndUsername(SearchCondition searchCondition, Pageable pageable);
    Slice<PostResponseDto> findLikesList(SearchCondition searchCondition, Pageable pageable);

    // 관리자 Posts 목록 - 임시글 포함, 제목/작성자 개별 검색
    Page<AdminPostResponseDto> findPostsForAdmin(String title, String author, Pageable pageable);
}
