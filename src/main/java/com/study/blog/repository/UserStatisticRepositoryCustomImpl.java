package com.study.blog.repository;

import static com.study.blog.entity.QPostTag.postTag;
import static com.study.blog.entity.QTag.tag;
import static com.study.blog.entity.QUserStatistic.userStatistic;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.dto.postTag.TagCountDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserCategoryStatisticDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import com.study.blog.entity.QPostTag;
import com.study.blog.entity.QTag;
import com.study.blog.entity.QUserStatistic;
import com.study.blog.entity.enums.StatisticType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserStatisticRepositoryCustomImpl implements UserStatisticRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public UserStatisticRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public SolveStatisticResponseDto getPostCountSummary(Long userId, String categoryType) {
        QUserStatistic us = userStatistic;
        QUserStatistic subUs = new QUserStatistic("subUs");

        return queryFactory
            .select(Projections.constructor(SolveStatisticResponseDto.class,
                us.categoryName,
                JPAExpressions
                    .select(subUs.statisticCount)
                    .from(subUs)
                    .where(
                        subUs.statisticType.eq(StatisticType.TOTAL),
                        subUs.categoryName.eq(categoryType),
                        subUs.user.id.eq(userId)
                        )
                    .limit(1),
                JPAExpressions
                    .select(subUs.statisticCount)
                    .from(subUs)
                    .where(
                        subUs.statisticType.eq(StatisticType.YEAR),
                        subUs.categoryName.eq(categoryType),
                        subUs.user.id.eq(userId)
                    )
                    .limit(1),
                JPAExpressions
                    .select(subUs.statisticCount)
                    .from(subUs)
                    .where(
                        subUs.statisticType.eq(StatisticType.MONTH),
                        subUs.categoryName.eq(categoryType),
                        subUs.user.id.eq(userId)
                    )
                    .limit(1)
            ))
            .from(us)
            .where(
                us.user.id.eq(userId),
                us.categoryName.eq(categoryType)
            )
            .limit(1)
            .fetchOne();
    }

    @Override
    public List<UserDailyStatisticDto> getDailyPostCount(Long userId, String categoryType) {
        QUserStatistic us = userStatistic;

        return queryFactory
            .select(Projections.constructor(UserDailyStatisticDto.class,
                us.statisticCount,
                us.statisticDate
            ))
            .from(us)
            .where(
                us.statisticType.eq(StatisticType.DAILY),
                us.user.id.eq(userId),
                us.categoryName.eq(categoryType)
            )
            .orderBy(us.statisticDate.asc())
            .fetch();
    }

    @Override
    public List<TagCountDto> getTagPostCount(Long userId, String categoryType) {
        QUserStatistic us = userStatistic;

        return queryFactory
            .select(Projections.constructor(TagCountDto.class,
                tag.id,
                tag.name,
                us.statisticCount
            ))
            .from(us)
            .leftJoin(tag).on(us.tagName.eq(tag.name))
            .where(
                us.statisticType.eq(StatisticType.TAG),
                us.user.id.eq(userId),
                us.categoryName.eq(categoryType)
            )
            .orderBy(us.statisticCount.desc())
            .fetch();
    }

    @Override
    public List<UserCategoryStatisticDto> getCategoryPostCount(Long userId) {
        QUserStatistic us = userStatistic;

        return queryFactory
            .select(Projections.constructor(UserCategoryStatisticDto.class,
                us.statisticCount,
                us.categoryName
            ))
            .from(us)
            .where(
                us.statisticType.eq(StatisticType.CATEGORY),
                us.user.id.eq(userId)
            )
            .orderBy(us.statisticCount.desc())
            .fetch();
    }
}
