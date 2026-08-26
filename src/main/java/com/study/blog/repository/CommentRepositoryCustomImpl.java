package com.study.blog.repository;

import static com.study.blog.entity.QComment.comment1;
import static com.study.blog.entity.QPost.post;
import static com.study.blog.entity.QUsers.users;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.admin.dto.AdminCommentResponseDto;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CommentRepositoryCustomImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    // 관리자 Comments 목록 - 대댓글까지 독립된 행으로 평탄화, postTitle은 join으로 N+1 없이 조회
    @Override
    public Page<AdminCommentResponseDto> findCommentsForAdmin(String content, String author, Pageable pageable) {
        List<AdminCommentResponseDto> contentList = queryFactory
            .select(Projections.constructor(AdminCommentResponseDto.class,
                comment1.id,
                comment1.comment,
                comment1.user.username,
                comment1.user.nickname,
                comment1.post.id,
                post.title,
                comment1.createdDate,
                comment1.parentComment.id,
                users.isDeleted.eq("y")
            ))
            .from(comment1)
            .leftJoin(comment1.user, users)
            .leftJoin(comment1.post, post)
            .where(
                contentContains(content),
                authorContains(author)
            )
            .orderBy(comment1.createdDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(comment1.id.count())
            .from(comment1)
            .leftJoin(comment1.user, users)
            .leftJoin(comment1.post, post)
            .where(
                contentContains(content),
                authorContains(author)
            )
            .fetchOne();

        return new PageImpl<>(contentList, pageable, total != null ? total : 0);
    }

    private BooleanExpression contentContains(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }

        return comment1.comment.containsIgnoreCase(content);
    }

    private BooleanExpression authorContains(String author) {
        if (!StringUtils.hasText(author)) {
            return null;
        }

        return comment1.user.username.containsIgnoreCase(author)
            .or(comment1.user.nickname.containsIgnoreCase(author));
    }
}
