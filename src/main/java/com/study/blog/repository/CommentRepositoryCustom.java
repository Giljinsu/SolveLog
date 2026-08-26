package com.study.blog.repository;

import com.study.blog.admin.dto.AdminCommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

    // 관리자 Comments 목록 - 대댓글 포함 평탄화, 내용/작성자 개별 AND 검색
    Page<AdminCommentResponseDto> findCommentsForAdmin(String content, String author, Pageable pageable);
}
