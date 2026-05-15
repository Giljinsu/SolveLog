package com.study.blog.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.batch.dto.RedisDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class UserStatisticTest {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private String userStatisticKey(Long userId) {return "user:statistic:" + userId.toString(); }

    @Test
    @DisplayName("유저별 통계 레디스 테스트")
    public void getStatisticTest() throws JsonProcessingException {
        //  userId : 52 테스트용 user

        String str = redisTemplate.opsForValue().get(userStatisticKey(52L));

        Map<String, SolveStatisticResponseDto> redisMap =
            objectMapper.readValue(
                str,
                new TypeReference<Map<String, SolveStatisticResponseDto>>() {}
        );

        SolveStatisticResponseDto responseDto = redisMap.get("전체");

        List<UserDailyStatisticDto> yearDaily = responseDto.getDailyStatistic()
            .stream()
            .filter(dto -> dto.getDate().getYear() == 2025)
            .toList();


        System.out.println(yearDaily);
    }
}
