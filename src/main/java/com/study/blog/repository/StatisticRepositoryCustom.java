package com.study.blog.repository;

import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepositoryCustom {

    // 카테고리별 전체, 년별, 월별 게시글 수
    List<SolveStatisticResponseDto> getUserPostCountSummary(String username);

    // year, username, categoryType 사용 다음엔 dto 대신 풀어서 사용하자
    // username, year로 일별 게시글 작성 수
    List<UserDailyStatisticDto> getDailySolveCountByYearAndUser(SolveStatisticRequestDto solveStatisticRequestDto);
}
