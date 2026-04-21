package com.study.blog.repository;

import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepositoryCustom {
    List<UserDailyStatisticDto> getDailySolveCountByYearAndUser(SolveStatisticRequestDto solveStatisticRequestDto);
}
