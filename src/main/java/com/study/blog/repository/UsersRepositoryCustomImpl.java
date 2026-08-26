package com.study.blog.repository;

import static com.study.blog.entity.QUsers.users;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.admin.dto.AdminUserResponseDto;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

public class UsersRepositoryCustomImpl implements UsersRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UsersRepositoryCustomImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<AdminUserResponseDto> findUsersForAdmin(String keyword, Pageable pageable) {
        List<AdminUserResponseDto> content = queryFactory
            .select(Projections.constructor(AdminUserResponseDto.class,
                users.id,
                users.username,
                users.nickname,
                users.role,
                users.createdDate
            ))
            .from(users)
            .where(
                notDeleted(),
                keywordContains(keyword)
            )
            .orderBy(users.createdDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(users.id.count())
            .from(users)
            .where(
                notDeleted(),
                keywordContains(keyword)
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression notDeleted() {
        return users.isDeleted.eq("n");
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return users.username.containsIgnoreCase(keyword)
            .or(users.nickname.containsIgnoreCase(keyword));
    }
}
