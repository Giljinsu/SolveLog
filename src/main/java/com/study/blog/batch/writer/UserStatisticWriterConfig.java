package com.study.blog.batch.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.batch.dto.RedisDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.entity.UserStatistic;
import com.study.blog.repository.UserStatisticJdbcRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@RequiredArgsConstructor
public class UserStatisticWriterConfig {
//    private final EntityManagerFactory entityManagerFactory;
    private final UserStatisticJdbcRepository statisticJdbcRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

//    @Bean
//    public JpaItemWriter<List<UserStatistic>> userStatisticWriter() {
//        return new JpaItemWriterBuilder<List<UserStatistic>>()
//            .entityManagerFactory(entityManagerFactory)
//            .build();
//    }

    @Bean
    public ItemWriter<List<UserStatistic>> userStatisticWriter() {
        return chunk -> {
            for (List<UserStatistic> statistics : chunk) {
                statisticJdbcRepository.bulkUpsert(statistics);
            }
        };
    }

    // 이부분 처리 redis
    @Bean
    public ItemWriter<RedisDto> redisWriter() {
        return chunk -> {
            for (RedisDto dto : chunk) {
                String value = objectMapper.writeValueAsString(
                    dto.getSolveStatisticResponseDtoMap()
                );

                stringRedisTemplate.opsForValue().set(
                    "user:statistic:" + dto.getUserId().toString(),
                    value,
                    Duration.ofHours(24)
                );
            }
        };
    }
}
