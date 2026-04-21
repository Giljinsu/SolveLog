package com.study.blog.repository;

import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserCategoryStatisticDto;
import com.study.blog.entity.Post;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom,
    StatisticRepositoryCustom {

    // 임시 게시글 목록
    @Query("select p from Post p where p.isTemp = true and p.user.id = :userId order by p.createdDate")
    List<Post> findByUserId(@Param("userId") Long userId);

    @Query("select count(p.id) from Post p where p.user.username = :username and p.isTemp is false ")
    Long getPostCountByUsername(@Param("username") String username);

    @Query(
        "select count(p.id) "
        + "from Post p "
        + "where p.user.username = :username "
            + " and p.category.type = :categoryType"
            + " and p.isTemp is false ")
    Long getPostCount(@Param("username") String username,
        @Param("categoryType") String categoryType);

    @Query(
        "select new com.study.blog.dto.statistic.UserCategoryStatisticDto(count(p.id), p.category.type)  "
            + "from Post p "
            + "where p.user.username = :username "
            + " and p.isTemp is false "
            + "group by p.category.type")
    List<UserCategoryStatisticDto> getPostCountOfCategory(@Param("username") String username);

    // 유저별 전체 개수 년별 월별
    @Query(
        "select new com.study.blog.dto.statistic.SolveStatisticResponseDto("
            + "count(p.id), "
            + "sum(case when year(p.createdDate) = :year then 1 else 0 end), "
            + "sum(case when year(p.createdDate) = :year "
            + "          and month(p.createdDate) = :month then 1 else 0 end) "
            + ")  "
            + "from Post p "
            + "where p.user.username = :username "
            + " and p.isTemp is false ")
    SolveStatisticResponseDto getUserStatistics(
        @Param("username") String username,
        @Param("year") int year,
        @Param("month") int month
//        @Param("currentDate") LocalDate currentDate
    );

    // 카테고리별 전체 개수 년별 월별
    @Query(
        "select new com.study.blog.dto.statistic.SolveStatisticResponseDto("
            + "count(p.id), "
            + "sum(case when year(p.createdDate) = :year then 1 else 0 end), "
            + "sum(case when year(p.createdDate) = :year "
            + "          and month(p.createdDate) = :month then 1 else 0 end) "
            + ")  "
            + "from Post p "
            + "where p.user.username = :username "
            + "and p.category.type = :categoryType "
            + "and p.isTemp is false ")
    SolveStatisticResponseDto getUserCategoryStatistics(
        @Param("username") String username,
        @Param("year") int year,
        @Param("month") int month,
//        @Param("currentDate") LocalDate currentDate,
        @Param("categoryType") String categoryType
    );
//    @Query("select p from Post p where p.user.username = :username and p.isTemp is true")
//    Slice<Post> findByUsernameAndIsTemp(@Param("username") String username, Pageable pageable);
}

