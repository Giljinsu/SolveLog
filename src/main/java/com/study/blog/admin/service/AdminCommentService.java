package com.study.blog.admin.service;

import com.study.blog.admin.dto.AdminCommentResponseDto;
import com.study.blog.repository.CommentRepository;
import com.study.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommentService {

    private final CommentRepository commentRepository;
    private final CommentService commentService;

    // 관리자 Comments 목록 - 대댓글 포함 평탄화, 내용/작성자 개별 검색
    public Page<AdminCommentResponseDto> getComments(String content, String author, Pageable pageable) {
        return commentRepository.findCommentsForAdmin(content, author, pageable);
    }

    // 관리자 Comment 삭제 - 작성자 검사 없이 삭제, 대댓글이 있으면 함께 삭제(기존 정책 유지)
    @Transactional
    public void deleteComment(Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}
