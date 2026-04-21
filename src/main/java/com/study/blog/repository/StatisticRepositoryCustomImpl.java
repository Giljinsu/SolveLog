package com.study.blog.repository;

import static com.study.blog.entity.QPost.*;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.dto.post.SearchCondition;
import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class StatisticRepositoryCustomImpl implements StatisticRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public StatisticRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
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
                    post.id.count().intValue(),
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

