package com.study.blog.batch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.dto.statistic.SolveStatisticRequestDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import com.study.blog.entity.Users;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.repository.UserStatisticRepository;
import com.study.blog.repository.UsersRepository;
import com.study.blog.service.StatisticService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserStatisticBatchService {
    private final UserStatisticRepository userStatisticRepository;
    private final UsersRepository usersRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final StatisticService statisticService; // 통계 조회 배치 아닌 단순 조회

    private String userStatisticKey(Long userId) {return "user:statistic:" + userId.toString(); }

    // 유저 통계 테이블에서의 유저 통계
    public SolveStatisticResponseDto getUserStatistic(Long userId, String categoryType) {
        SolveStatisticResponseDto responseDto = userStatisticRepository.getPostCountSummary(
            userId, categoryType);

        if (responseDto == null) {
            responseDto = new SolveStatisticResponseDto();
        }

        responseDto.setDailyStatistic(userStatisticRepository.getDailyPostCount(userId, categoryType));
        responseDto.setTagStatistic(userStatisticRepository.getTagPostCount(userId, categoryType));
        responseDto.setCategoryStatistic(userStatisticRepository.getCategoryPostCount(userId));

        return responseDto;
    }


    // 레디스에 저장된 유저 통계
    public SolveStatisticResponseDto getUserStatisticFromRedis(SolveStatisticRequestDto requestDto) {
        Users users = usersRepository.findUsersByUsernameIsNotDeleted(requestDto.getUsername())
            .orElseThrow(
                NotExistUserException::new);

        Long userId = users.getId();
        Integer year = requestDto.getYear();
        String categoryType = requestDto.getCategoryType();
        if(categoryType == null) categoryType = "전체";

        try {
            // 레디스가 저장 안된 사용자 즉 신규 사용자 혹은 어떤 이유로 레디스 작동 안됨 -> 일때 처리 가존 통계 서비스 사용
            String value = redisTemplate.opsForValue().get(userStatisticKey(userId));

            if (value == null) {
                return statisticService.getUserStatistic(requestDto);
            }

            Map<String, SolveStatisticResponseDto> redisMap =
                objectMapper.readValue(
                    value,
                    new TypeReference<Map<String, SolveStatisticResponseDto>>() {}
                );

            SolveStatisticResponseDto responseDto = redisMap.get(categoryType);

            if (responseDto == null) {
                return statisticService.getUserStatistic(requestDto);
            }

            List<UserDailyStatisticDto> yearDaily = responseDto.getDailyStatistic()
                .stream()
                .filter(dto -> dto.getDate().getYear() == year)
                .toList();

            responseDto.setDailyStatistic(yearDaily);

            return responseDto;
        } catch (Exception e) {
            log.warn("통계 Redis 조회 실패. DB 조회로 fallback. userId={}", userId, e);
            return statisticService.getUserStatistic(requestDto);
        }

    }

    public List<UserDailyStatisticDto> getDailyStatistic(SolveStatisticRequestDto requestDto) {
        Users users = usersRepository.findUsersByUsernameIsNotDeleted(requestDto.getUsername())
            .orElseThrow(
                NotExistUserException::new);

        Long userId = users.getId();
        Integer year = requestDto.getYear();
        String categoryType = requestDto.getCategoryType();
        if(categoryType == null) categoryType = "전체";

        try {
            // 레디스가 저장 안된 사용자 즉 신규 사용자 혹은 어떤 이유로 레디스 작동 안됨 -> 일때 처리 가존 통계 서비스 사용
            String value = redisTemplate.opsForValue().get(userStatisticKey(userId));

            if (value == null) {
                return statisticService.getDailySolveCountByYearAndUser(requestDto);
            }

            Map<String, SolveStatisticResponseDto> redisMap =
                objectMapper.readValue(
                    value,
                    new TypeReference<Map<String, SolveStatisticResponseDto>>() {}
                );

            SolveStatisticResponseDto responseDto = redisMap.get(categoryType);

            if (responseDto == null) {
                return statisticService.getDailySolveCountByYearAndUser(requestDto);
            }

            return responseDto.getDailyStatistic()
                .stream()
                .filter(dto -> dto.getDate().getYear() == year)
                .toList();

        } catch (Exception e) {
            log.warn("통계 Redis 조회 실패. DB 조회로 fallback. userId={}", userId, e);
            return statisticService.getDailySolveCountByYearAndUser(requestDto);
        }
    }

}
