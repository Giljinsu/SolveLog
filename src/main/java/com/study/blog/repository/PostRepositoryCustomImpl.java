package com.study.blog.repository;

import static com.study.blog.entity.QCategory.*;
import static com.study.blog.entity.QComment.*;
import static com.study.blog.entity.QFile.*;
import static com.study.blog.entity.QLikes.likes;
import static com.study.blog.entity.QPost.*;
import static com.study.blog.entity.QPostTag.*;
import static com.study.blog.entity.QUsers.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.blog.dto.comment.CommentResponseDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.dto.post.SearchCondition;
import com.study.blog.dto.post.SearchOrderType;
import com.study.blog.dto.post.SearchType;
import com.study.blog.entity.PostTag;
import com.study.blog.entity.QCategory;
import com.study.blog.entity.QComment;
import com.study.blog.entity.QFile;
import com.study.blog.entity.QPostTag;
import com.study.blog.entity.QUsers;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;



    public PostRepositoryCustomImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Slice<PostResponseDto> findListWithSearchCondition(SearchCondition searchCondition, Pageable pageable) {
        List<PostResponseDto> content = getPostListFetch(searchCondition, pageable);
        return getSlicePostList(searchCondition, pageable, content);
    }

    @Override
    public Page<PostResponseDto> findListWithSearchCondition_Page(SearchCondition searchCondition, Pageable pageable) {
        List<PostResponseDto> content = getPostListFetch(searchCondition, pageable);

        Integer total = queryFactory
            .select(post.id.count().intValue())
            .from(post)
            .where(
                searchCondition(searchCondition)
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public Slice<PostResponseDto> findLikesList(SearchCondition searchCondition, Pageable pageable) {
        List<PostResponseDto> content = getLikesPostListFetch(searchCondition, pageable);
        return getSlicePostList(searchCondition, pageable, content);
    }

    private List<PostResponseDto> getPostListFetch(SearchCondition searchCondition, Pageable pageable) {
        JPAQuery<PostResponseDto> query = queryFactory
            .select(Projections.constructor(PostResponseDto.class,
                    post.id,
                    post.title,
                    post.content,
                    post.createdDate,
                    post.viewCount,
                    post.category.type,
                    likes.id.countDistinct().intValue(),
//                JPAExpressions.select(likes.id.count().intValue())
//                    .from(likes)
//                    .where(likes.post.id.eq(post.id)),
                    comment1.id.countDistinct().intValue(),
//                JPAExpressions.select(comment1.count().intValue())
//                    .from(comment1)
//                    .where(comment1.post.id.eq(post.id)),
                    post.user.nickname,
                    post.user.username,
                    post.tags,
                    post.summary,
                    Projections.constructor(FileResponseDto.class,
                        file.id,
                        file.postId,
                        file.type,
                        file.originalFileName,
                        file.size,
                        file.isThumbnail
                    )
                )
            )
            .from(post)
            .leftJoin(post.user, users)
            .leftJoin(likes).on(likes.post.id.eq(post.id))
            .leftJoin(comment1).on(comment1.post.id.eq(post.id))
            .leftJoin(post.category, category)
            .leftJoin(file).on(file.postId.eq(post.id)
                .and(file.isThumbnail.isTrue()));

        // 태그 검색일 경우
//        if (searchCondition.getTagId() != null) {
//            query.leftJoin(postTag).on(postTag.post.id.eq(post.id));
//        }
        if (searchCondition.getTagIdList() != null && !searchCondition.getTagIdList().isEmpty()) {
            query.leftJoin(postTag).on(postTag.post.id.eq(post.id));
        }

        query.where(
            searchCondition(searchCondition),
            Boolean.TRUE.equals(searchCondition.getIsTemp()) ? post.isTemp.isTrue()
                : post.isTemp.isFalse()
            )
            .groupBy(
                post.id,
                post.title,
                post.content,
                post.createdDate,
                post.viewCount,
                post.category.type,
                post.user.nickname,
                post.user.username,
                post.tags,
                post.summary,
                file.id
            );

        if (searchCondition.getTagIdList() != null && !searchCondition.getTagIdList().isEmpty()) {
            query.having(postTag.tag.id.countDistinct().eq((long) searchCondition.getTagIdList().size()));
        }

        return query
            .orderBy(
                searchType(searchCondition),
                post.createdDate.desc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1) // Slice 용
            .fetch();

    }

    private List<PostResponseDto> getLikesPostListFetch(SearchCondition searchCondition, Pageable pageable) {
        JPAQuery<PostResponseDto> query = queryFactory
            .select(Projections.constructor(PostResponseDto.class,
                    post.id,
                    post.title,
                    post.content,
                    post.createdDate,
                    post.viewCount,
                    post.category.type,
                    likes.id.countDistinct().intValue(),
//                JPAExpressions.select(likes.id.count().intValue())
//                    .from(likes)
//                    .where(likes.post.id.eq(post.id)),
                    comment1.id.countDistinct().intValue(),
//                JPAExpressions.select(comment1.count().intValue())
//                    .from(comment1)
//                    .where(comment1.post.id.eq(post.id)),
                    post.user.nickname,
                    post.user.username,
                    post.tags,
                    post.summary,
                    Projections.constructor(FileResponseDto.class,
                        file.id,
                        file.postId,
                        file.type,
                        file.originalFileName,
                        file.size,
                        file.isThumbnail
                    )
                )
            )
            .from(likes)
            .leftJoin(post).on(likes.post.id.eq(post.id))
            .leftJoin(post.user, users)
            .leftJoin(comment1).on(comment1.post.id.eq(post.id))
            .leftJoin(post.category, category)
            .leftJoin(file).on(file.postId.eq(post.id)
                .and(file.isThumbnail.isTrue()));

        // 태그 검색일 경우
        if (searchCondition.getTagIdList() != null && !searchCondition.getTagIdList().isEmpty()) {
            query.leftJoin(postTag).on(postTag.post.id.eq(post.id));
        }

        query.where(
                searchLikesUsername(searchCondition),
                searchTagId(searchCondition),
                searchTypeCondition(searchCondition),
                post.isTemp.isFalse()
            )
            .groupBy(
                post.id,
                post.title,
                post.content,
                post.createdDate,
                post.viewCount,
                post.category.type,
                post.user.nickname,
                post.user.username,
                post.tags,
                post.summary,
                file.id
            );

        if (searchCondition.getTagIdList() != null && !searchCondition.getTagIdList().isEmpty()) {
            query.having(postTag.tag.id.countDistinct().eq((long) searchCondition.getTagIdList().size()));
        }

        return query
            .orderBy(
//                searchType(searchCondition),
                post.createdDate.desc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1) // Slice 용
            .fetch();

    }

    @Override
    public PostResponseDto findDetailPostById(Long postId) {
        return queryFactory
            .select(Projections.constructor(PostResponseDto.class,
                    post.id,
                    post.title,
                    post.content,
                    post.createdDate,
                    post.viewCount,
                    post.category.type,
                    likes.id.countDistinct().intValue(),
//                    JPAExpressions.select(likes.id.count().intValue())
//                        .from(likes)
//                        .where(likes.post.id.eq(post.id)),
                    comment1.id.countDistinct().intValue(),
//                    JPAExpressions.select(comment1.count().intValue())
//                        .from(comment1)
//                        .where(comment1.post.id.eq(post.id)),
                    post.user.nickname,
                    post.user.username,
                    post.tags,
                    post.summary,
                    post.isTemp
                )
            )
            .from(post)
            .leftJoin(post.user, users)
            .leftJoin(likes).on(post.id.eq(likes.post.id))
            .leftJoin(comment1).on(comment1.post.id.eq(post.id))
            .where(
                post.id.eq(postId)
            )
            .groupBy(
                post.id,
                post.title,
                post.content,
                post.createdDate,
                post.viewCount,
                post.category.type,
                post.user.nickname,
                post.user.username,
                post.tags,
                post.summary
            ).fetchOne();
    }


    // 사용 안함
    @Override
    public Slice<PostResponseDto> findByUsernameAndIsTemp(String username, Pageable pageable) {

        SearchCondition searchCondition = new SearchCondition();
        searchCondition.setUsername(username);
        searchCondition.setIsTemp(true);

        List<PostResponseDto> content = getPostListFetch(searchCondition, pageable);

        return getSlicePostList(searchCondition, pageable, content);
    }

    private Slice<PostResponseDto> getSlicePostList(SearchCondition searchCondition, Pageable pageable,
        List<PostResponseDto> content) {

        boolean hasNext = false;

        // 사이즈 + 1로 다음 페이지가 있는지 판단
        if (content.size() > pageable.getPageSize())  {
            hasNext = true;
            content.removeLast();
        }


        return new SliceImpl<>(content,pageable,hasNext);
    }

    @Override
    public Slice<PostResponseDto> getPostByTagIdAndUsername(SearchCondition searchCondition, Pageable pageable) {
        if (searchCondition.getUsername().isEmpty()) {
            throw new UsernameNotFoundException("can't find matched user");
        }
//        if (searchCondition.getTagId() == null) {
//            throw new IllegalStateException("can't find tagId");
//        }

        List<PostResponseDto> content = getPostListFetch(searchCondition, pageable);

        return getSlicePostList(searchCondition, pageable, content);
    }


    // orderType
    private OrderSpecifier<?> searchType(SearchCondition searchCondition) {

        if (searchCondition.getSearchOrderType() == null) {
            return post.createdDate.desc();
        }

        // 조회수, 최신순, 좋아요순
        switch (searchCondition.getSearchOrderType()) {
            case SearchOrderType.LATEST -> {
                return post.createdDate.desc();
            }
            case SearchOrderType.VIEWS -> {
                return post.viewCount.desc();
            }
            case SearchOrderType.LIKE -> {
//                return new OrderSpecifier<>(
//                    Order.DESC,
//                    JPAExpressions.select(likes.id.count().intValue())
//                        .from(likes)
//                        .where(likes.post.id.eq(post.id))
//                );


                return likes.id.countDistinct().desc();
            }
        }
        return post.createdDate.desc();
    }

    private Predicate searchCondition(SearchCondition searchCondition) {
        // 검색조건 -> 제목, 내용, 글쓴이, 제목과 글내용
        BooleanBuilder builder = new BooleanBuilder();
        if (searchCondition.getCategoryType() != null) {
            builder.and(searchCategory(searchCondition));
        }
//        if (searchCondition.getTagId() != null) {
//            builder.and(searchTagId(searchCondition));
//        }
        if (searchCondition.getTagIdList() != null && !searchCondition.getTagIdList().isEmpty()) {
            searchCondition.getTagIdList().forEach(tagId -> builder.and(searchTagId(searchCondition)));
        }
        if (StringUtils.hasText(searchCondition.getUsername())) {
            builder.and(searchUsername(searchCondition));
        }
        if (searchCondition.getSearchType() == null) {
            return builder.hasValue() ? builder.getValue() : null;
        }
        BooleanBuilder searchTypeBuilder = searchTypeCondition(searchCondition);
        builder.and(searchTypeBuilder);

        return builder.hasValue() ? builder.getValue() : null;
    }

    private BooleanBuilder searchTypeCondition(SearchCondition searchCondition) {
        if (searchCondition.getSearchType() == null) return null;

        BooleanBuilder builder = new BooleanBuilder();
        switch (searchCondition.getSearchType()) {
            case SearchType.TITLE -> builder.and(searchTitle(searchCondition));
            case SearchType.CONTENT -> builder.and(searchContent(searchCondition));
            case SearchType.USERNAME -> builder.and(searchUser(searchCondition));
            case TITLE_AND_CONTENT -> {
                builder.and(searchTitle(searchCondition));
                builder.or(searchContent(searchCondition));
            }
        }
        return builder;
    }

    private BooleanExpression searchCategory(SearchCondition searchCondition) {
        if (searchCondition.getCategoryType().isEmpty()) {
            return null;
        }

        return post.category.type.eq(searchCondition.getCategoryType());
    }

    private BooleanExpression searchUser(SearchCondition searchCondition) {
        if (searchCondition.getSearchValue().isEmpty()) {
            return null;
        }

        return post.user.nickname.toUpperCase().contains(searchCondition.getSearchValue().toUpperCase());
    }

    private BooleanExpression searchContent(SearchCondition searchCondition) {
        if (searchCondition.getSearchValue().isEmpty()) {
            return null;
        }

//        return post.content.toUpperCase().contains(searchCondition.getSearchValue().toUpperCase());
        return post.content.containsIgnoreCase(searchCondition.getSearchValue());
    }

    private BooleanExpression searchTitle(SearchCondition searchCondition) {
        if (searchCondition.getSearchValue().isEmpty()) {
            return null;
        }

        return post.title.toUpperCase().contains(searchCondition.getSearchValue().toUpperCase());
    }

//    private BooleanExpression searchTagId(SearchCondition searchCondition) {
//        if (searchCondition.getTagId() == null) {
//            return null;
//        }
//
//        return postTag.tag.id.eq(searchCondition.getTagId());
//    }

    private BooleanExpression searchTagId(SearchCondition searchCondition) {
        if (searchCondition.getTagIdList().isEmpty()) {
            return null;
        }

        return postTag.tag.id.in(searchCondition.getTagIdList());
    }

    private BooleanExpression searchUsername(SearchCondition searchCondition) {
        if (searchCondition.getUsername().isEmpty()) {
          return null;
        }

        return post.user.username.eq(searchCondition.getUsername());
    }

    private BooleanExpression searchLikesUsername(SearchCondition searchCondition) {
        if (searchCondition.getUsername().isEmpty()) {
            return null;
        }

        return likes.user.username.eq(searchCondition.getUsername());
    }
}
