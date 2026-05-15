package com.study.blog.dto.statistic;

import java.sql.Date;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDailyStatisticDto {
    private long count;
    private LocalDate date;

    public UserDailyStatisticDto(long count, Date date) {
        this.count = count;
        this.date = date.toLocalDate();
    }
}
