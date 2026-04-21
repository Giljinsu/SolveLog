package com.study.blog.controller;

import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import com.study.blog.service.StatisticService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    // 유저별 년도별 일일 게시글 수 통계
    @GetMapping("/api/statistic/getDailySolveCountByYearAndUser")
    public Result<List<UserDailyStatisticDto>> getDailySolveCountByYearAndUser(
        @ModelAttribute SolveStatisticRequestDto solveStatisticRequestDto) {
        return Result.of(statisticService.getDailySolveCountByYearAndUser(solveStatisticRequestDto));
    }

    // 통계
    @GetMapping("/api/statistic/getUserStatistic")
    public SolveStatisticResponseDto getUserStatistic(
        @ModelAttribute SolveStatisticRequestDto requestDto) {
        return statisticService.getUserStatistic(requestDto);
    }
}
