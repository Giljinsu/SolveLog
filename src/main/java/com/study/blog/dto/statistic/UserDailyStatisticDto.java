package com.study.blog.dto.statistic;

import java.sql.Date;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDailyStatisticDto {
    private int count;
    private LocalDate date;

    public UserDailyStatisticDto(int count, Date date) {
        this.count = count;
        this.date = date.toLocalDate();
    }
}
