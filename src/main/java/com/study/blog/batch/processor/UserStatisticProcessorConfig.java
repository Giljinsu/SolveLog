package com.study.blog.batch.processor;

import com.study.blog.batch.dto.RedisDto;
import com.study.blog.batch.service.UserStatisticBatchService;
import com.study.blog.dto.postTag.TagCountDto;
import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserCategoryStatisticDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import com.study.blog.entity.Category;
import com.study.blog.entity.UserStatistic;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.StatisticType;
import com.study.blog.repository.CategoryRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.PostTagRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UserStatisticProcessorConfig {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostTagRepository postTagRepository;
    private final UserStatisticBatchService batchService;


    // 전체, 년별, 일별 게시글 수 통계
    @Bean
    public ItemProcessor<Users, List<UserStatistic>> postCountSummaryProcessor() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        return user -> {
            // 카테고리 없이 전체 게시글 통계
            SolveStatisticResponseDto userStatistics = postRepository.getUserStatistics(
                user.getUsername(),
                year,
                month
            );

            if (userStatistics == null || userStatistics.getTotalCount() == 0) {
                return null;
            }

            userStatistics.setCategoryType("전체");

            //카테고리 별 게시글 통계
            List<SolveStatisticResponseDto> userPostCountSummaryList = postRepository.getUserPostCountSummary(
                user.getUsername());

            if(userPostCountSummaryList == null) {
                userPostCountSummaryList = new ArrayList<>();
            }

            userPostCountSummaryList.add(userStatistics);


            List<UserStatistic> userStatisticList = new ArrayList<>();
            for (SolveStatisticResponseDto userPostCountSummary : userPostCountSummaryList) {
                UserStatistic totalStatistic = UserStatistic.createUserStatistic(user,
                    StatisticType.TOTAL);
                UserStatistic yearStatistic = UserStatistic.createUserStatistic(user,
                    StatisticType.YEAR);
                UserStatistic monthStatistic = UserStatistic.createUserStatistic(user,
                    StatisticType.MONTH);

                totalStatistic.updateUserStatistic(
                    userPostCountSummary.getCategoryType(),
                    null,
                    userPostCountSummary.getTotalCount(),
                    null
                );
                yearStatistic.updateUserStatistic(
                    userPostCountSummary.getCategoryType(),
                    null,
                    userPostCountSummary.getYearCount(),
                    LocalDate.of(year, 1, 1)
                );
                monthStatistic.updateUserStatistic(
                    userPostCountSummary.getCategoryType(),
                    null,
                    userPostCountSummary.getMonthCount(),
                    LocalDate.of(year, month, 1)
                );

                userStatisticList.add(totalStatistic);
                userStatisticList.add(yearStatistic);
                userStatisticList.add(monthStatistic);
            }

            return userStatisticList;
        };
    }

    // 이번 년도 일별 게시글 수
    @Bean
    @StepScope // jobParameter를 받으려면 필요
    public ItemProcessor<Users, List<UserStatistic>> dailyPostCountProcessor(
        @Value("#{jobParameters['year']}") Integer year
    ) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : LocalDate.now().getYear();

        //SEARCH_CATEGORY = 게시판 카테고리
        Category parentCategory = categoryRepository.findByType("SEARCH_CATEGORY");
        List<Category> categoryList = categoryRepository.findByParentCategoryIdOrderBySortOrder(
            parentCategory.getId());

        Set<String> categoryTypeSet = categoryList.stream().map(category -> category.getType())
            .collect(Collectors.toSet());
        categoryTypeSet.add(null); // 전체 추가 null 처리로 해당 리포에서 전체를 가져옴

        return user -> {
            List<UserStatistic> userStatisticList = new ArrayList<>();
            for (String categoryType : categoryTypeSet) {
                // year, username, category type 사용.
                SolveStatisticRequestDto requestDto = new SolveStatisticRequestDto();
                requestDto.setYear(targetYear);
                requestDto.setUsername(user.getUsername());
                requestDto.setCategoryType(categoryType);

                List<UserDailyStatisticDto> dailySolveCountByYearAndUser =
                    postRepository.getDailySolveCountByYearAndUser(requestDto);

                for (UserDailyStatisticDto dailyStatistic : dailySolveCountByYearAndUser) {
                    UserStatistic userStatistic = UserStatistic.createUserStatistic(user,
                        StatisticType.DAILY);

                    userStatistic.updateUserStatistic(
                        categoryType == null ? "전체" : categoryType,
                        null,
                        dailyStatistic.getCount(),
                        dailyStatistic.getDate()
                    );

                    userStatisticList.add(userStatistic);
                }

            }
            return userStatisticList;
        };
    }

    //태그별 통계
    @Bean
    public ItemProcessor<Users, List<UserStatistic>> tagPostCountProcessor() {
        //SEARCH_CATEGORY = 게시판 카테고리
        Category parentCategory = categoryRepository.findByType("SEARCH_CATEGORY");
        List<Category> categoryList = categoryRepository.findByParentCategoryIdOrderBySortOrder(
            parentCategory.getId());

        Set<String> categoryTypeSet = categoryList.stream().map(category -> category.getType())
            .collect(Collectors.toSet());
        categoryTypeSet.add("전체"); // 전체 추가

        return user -> {
            // 카테고리 전체
            List<UserStatistic> userStatisticList = new ArrayList<>();
            for (String categoryType : categoryTypeSet) {
                List<TagCountDto> tagCountByUsername;
                if (categoryType.equals("전체")) {
                    tagCountByUsername = postTagRepository.findTagCountByUsername(user.getUsername());
                } else {
                    tagCountByUsername = postTagRepository.findTagCountByUsernameAndCategory(
                        user.getUsername(), categoryType);
                }

                for (TagCountDto tagCount : tagCountByUsername) {
                    UserStatistic userStatistic = UserStatistic.createUserStatistic(user,
                        StatisticType.TAG);

                    userStatistic.updateUserStatistic(
                        categoryType,
                        tagCount.getTagName(),
                        tagCount.getCount(),
                        null);

                    userStatisticList.add(userStatistic);
                }
            }

            return  userStatisticList;
        };
    }

    //카테고리별 통계
    @Bean
    public ItemProcessor<Users, List<UserStatistic>> categoryPostCountProcessor() {
        return user -> {
            List<UserCategoryStatisticDto> postCountOfCategory = postRepository.getPostCountOfCategory(
                user.getUsername());

            List<UserStatistic> userStatisticList = new ArrayList<>();
            for (UserCategoryStatisticDto categoryStatistic : postCountOfCategory) {
                UserStatistic userStatistic = UserStatistic.createUserStatistic(user,
                    StatisticType.CATEGORY);

                userStatistic.updateUserStatistic(
                    categoryStatistic.getCategoryType(),
                    null,
                    categoryStatistic.getCount(),
                    null
                );

                userStatisticList.add(userStatistic);
            }

            return userStatisticList;
        };
    }

    // Redis 저장
    @Bean
    public ItemProcessor<Users, RedisDto> redisStatisticProcessor() {
        //SEARCH_CATEGORY = 게시판 카테고리
        Category parentCategory = categoryRepository.findByType("SEARCH_CATEGORY");
        List<Category> categoryList = categoryRepository.findByParentCategoryIdOrderBySortOrder(
            parentCategory.getId());

        Set<String> categoryTypeSet = categoryList.stream().map(category -> category.getType())
            .collect(Collectors.toSet());
        categoryTypeSet.add("전체"); // 전체 추가

        return user -> {
            // 카테고리 전체
            Map<String, SolveStatisticResponseDto> userStatisticMap = new HashMap<>();

            for (String categoryType : categoryTypeSet) {
                SolveStatisticResponseDto userStatistic = batchService.getUserStatistic(
                    user.getId(), categoryType);


                userStatisticMap.put(categoryType, userStatistic);
            }

            RedisDto redisDto = new RedisDto();
            redisDto.setUserId(user.getId());
            redisDto.setSolveStatisticResponseDtoMap(userStatisticMap);

            return redisDto;
        };
    }
}
