package com.study.blog.ai.thumbnail.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.ai.thumbnail.domain.ThumbnailJob;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ThumbnailJobRepository {
    private static final String JOB_KEY_PREFIX = "thumbnail:job:";
    private static final String QUEUE_KEY = "thumbnail:queue";
    private static final Duration JOB_TTL = Duration.ofHours(24);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void saveJob(ThumbnailJob job) {
        try {
            String value = objectMapper.writeValueAsString(job);
            stringRedisTemplate.opsForValue().set(jobKey(job.getJobId()), value, JOB_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ThumbnailJob> findJob(String jobId) {
        String value = stringRedisTemplate.opsForValue().get(jobKey(jobId));

        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, ThumbnailJob.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void pushQueue(String jobId) {
        stringRedisTemplate.opsForList().rightPush(QUEUE_KEY, jobId);
    }

    // Redis List의 원자적 pop 연산 사용 - 별도 분산 락 불필요
    public Optional<String> popQueue() {
        return Optional.ofNullable(stringRedisTemplate.opsForList().leftPop(QUEUE_KEY));
    }

    private String jobKey(String jobId) {
        return JOB_KEY_PREFIX + jobId;
    }
}
