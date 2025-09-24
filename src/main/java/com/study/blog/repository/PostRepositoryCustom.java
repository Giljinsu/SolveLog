package com.study.blog.repository;

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
}
