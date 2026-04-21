package com.study.blog.service;

import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.PostTagRepository;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticService {
    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;

    // 년도별, 유저별 일일 게시글 작성개수
    public List<UserDailyStatisticDto> getDailySolveCountByYearAndUser(SolveStatisticRequestDto requestDto) {
        return postRepository.getDailySolveCountByYearAndUser(requestDto);
    }

    // 통계 조회
    public SolveStatisticResponseDto getUserStatistic(SolveStatisticRequestDto requestDto) {
        SolveStatisticResponseDto userStatistics;
        String username = requestDto.getUsername();
        LocalDate currentDate = requestDto.getCurrentDate();
        String categoryType = requestDto.getCategoryType();

        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        //카테고리 없는 전체 일 경우
        if (requestDto.getCategoryType() == null) {
            userStatistics = postRepository.getUserStatistics(
                username,
                year,
                month
            );

            // 태그 통계
            userStatistics.setTagStatistic(postTagRepository.findTagCountByUsernameOrderByCount(username));
        } else {
            userStatistics = postRepository.getUserCategoryStatistics(
                username,
                year,
                month,
                categoryType
            );

            // 태그 통계
            userStatistics.setTagStatistic(postTagRepository.findTagCountByUsernameAndCategory(
                username,
                categoryType
            ));
        }

        // 카테고리 통계
        userStatistics.setCategoryStatistic(postRepository.getPostCountOfCategory(username));

        // 일일 통계
        userStatistics.setDailyStatistic(
            postRepository.getDailySolveCountByYearAndUser(requestDto)
        );

        return userStatistics;
    }


}
