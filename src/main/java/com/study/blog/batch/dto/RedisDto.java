package com.study.blog.batch.dto;

import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RedisDto {
    private Long userId;
    private Map<String, SolveStatisticResponseDto> solveStatisticResponseDtoMap;
}
