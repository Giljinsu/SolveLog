package com.study.blog.repository;

import static com.study.blog.entity.QPost.*;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class StatisticRepositoryCustomImpl implements StatisticRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StatisticRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 카테고리별 전체, 년별, 월별 게시글 수
    @Override
    public List<SolveStatisticResponseDto> getUserPostCountSummary(String username) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        CaseBuilder caseBuilder = new CaseBuilder();
        NumberExpression<Integer> sum = caseBuilder.when(post.createdDate.year().eq(year)).then(1)
            .otherwise(0).sum();
        return queryFactory
            .select(
                Projections.constructor(SolveStatisticResponseDto.class,
                    post.category.type,
                    post.id.count(),
                    new CaseBuilder()
                        .when(post.createdDate.year().eq(year))
                        .then(1L)
                        .otherwise(0L)
                        .sum(),
                    new CaseBuilder()
                        .when(post.createdDate.year().eq(year)
                            .and(post.createdDate.month().eq(month)))
                        .then(1L)
                        .otherwise(0L)
                        .sum()
                )
            ).from(post)
            .where(
                post.user.username.eq(username),
                post.isTemp.isFalse()
            )
            .groupBy(post.category.type)
            .fetch();
    }

    @Override
    public List<UserDailyStatisticDto> getDailySolveCountByYearAndUser(SolveStatisticRequestDto requestDto) {
        int year = (requestDto.getYear() == null)
            ? requestDto.getCurrentDate().getYear()
            : requestDto.getYear();

        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year+1, 1, 1).atStartOfDay();
        String username = requestDto.getUsername();

        DateTemplate<Date> dateDateTemplate = Expressions.dateTemplate(Date.class, "date({0})",
            post.createdDate);

        return queryFactory
            .select(
                Projections.constructor(UserDailyStatisticDto.class,
                    post.id.count(),
                    dateDateTemplate
                )
            )
            .from(post)
            .where(
                post.user.username.eq(username),
//                post.createdDate.between(start, end),
                post.createdDate.goe(start).and(post.createdDate.lt(end)),
                searchCategory(requestDto),
                post.isTemp.isFalse()
            )
            .groupBy(
                dateDateTemplate
            ).fetch();

    }

    private BooleanExpression searchCategory(SolveStatisticRequestDto requestDto) {
        if (requestDto.getCategoryType() == null || requestDto.getCategoryType().isEmpty()) {
            return null;
        }

        return post.category.type.eq(requestDto.getCategoryType());
    }
}

