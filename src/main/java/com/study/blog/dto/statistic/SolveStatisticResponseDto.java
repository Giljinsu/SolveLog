package com.study.blog.dto.statistic;

import com.study.blog.dto.postTag.TagCountDto;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SolveStatisticResponseDto {
    // 추가
    private String categoryType;
    private Long totalCount;
    private Long yearCount;
    private Long monthCount;

    private List<UserCategoryStatisticDto> categoryStatistic;
    private List<UserDailyStatisticDto> dailyStatistic;
    private List<TagCountDto> tagStatistic;


    public SolveStatisticResponseDto(String categoryType, Long totalCount, Long yearCount, Long monthCount) {
        this.categoryType = categoryType;
        this.totalCount = totalCount;
        this.yearCount = yearCount;
        this.monthCount = monthCount;
    }

    public SolveStatisticResponseDto(Long totalCount, Long yearCount, Long monthCount) {
        this.totalCount = totalCount;
        this.yearCount = yearCount;
        this.monthCount = monthCount;
    }
}
