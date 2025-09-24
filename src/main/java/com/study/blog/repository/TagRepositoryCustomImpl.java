package com.study.blog.repository;

import static com.study.blog.entity.QPostTag.postTag;
import static com.study.blog.entity.QTag.*;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.dto.tag.TagResponseDto;
import com.study.blog.entity.QPostTag;
import com.study.blog.entity.QTag;
import jakarta.persistence.EntityManager;
import java.util.List;

public class TagRepositoryCustomImpl implements TagRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public TagRepositoryCustomImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<TagResponseDto> getTagAutoComplete(String value) {
        String lowerCase = value.toLowerCase();

        /*
            순서
            1. 정확한 순서
            2. a %
            3. %a%
         */

        NumberExpression<Integer> matchRank = new CaseBuilder()
            .when(tag.name.toLowerCase().eq(lowerCase)).then(3)
            .when(tag.name.toLowerCase().like(lowerCase + "%")).then(2)
            .when(tag.name.toLowerCase().like("%" + lowerCase + "%")).then(1)
            .otherwise(0);

        return queryFactory
            .select(Projections.constructor(TagResponseDto.class,
                tag.id,
                tag.name,
                postTag.post.id.countDistinct().intValue()
            ))
            .from(tag)
            .leftJoin(postTag).on(postTag.tag.id.eq(tag.id))
            .where(tag.name.toLowerCase().like("%" + lowerCase + "%"))
            .groupBy(tag.id, tag.name)
            .orderBy(matchRank.desc(),
                postTag.post.id.countDistinct().desc(),
                tag.name.asc())
            .limit(5)
            .fetch();
    }
}
