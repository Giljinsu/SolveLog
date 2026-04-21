package com.study.blog.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCategoryStatisticDto {
    private Long count;
    private String categoryType;
}
