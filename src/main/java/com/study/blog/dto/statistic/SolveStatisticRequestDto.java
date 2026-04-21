package com.study.blog.dto.statistic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SolveStatisticRequestDto {
    private String username;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDate currentDate;
    private Integer year;
    private String categoryType;
}
