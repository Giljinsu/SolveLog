package com.study.blog.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCategoryStatisticDto {
    private Long count;
    private String categoryType;
}
