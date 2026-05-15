package com.study.blog.batch.config;

import com.study.blog.batch.dto.RedisDto;
import com.study.blog.batch.processor.UserStatisticProcessorConfig;
import com.study.blog.batch.reader.UserStatisticReaderConfig;
import com.study.blog.batch.writer.UserStatisticWriterConfig;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.entity.UserStatistic;
import com.study.blog.entity.Users;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class StatisticBatchConfiguration {
    private final int userStatisticChunkSize = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserStatisticReaderConfig userListReader;
    private final UserStatisticProcessorConfig statisticProcessor;
    private final UserStatisticWriterConfig statisticWriter;


    @Bean
    public Job statisticJob(
        Step summaryStatisticStep,
        Step dailyPostCountStep,
        Step tagPostCountStep,
        Step categoryPostCountStep,
        Step userStatisticRedisStep
    ) {
        return new JobBuilder("statisticJob", jobRepository)
            .start(summaryStatisticStep)
            .next(dailyPostCountStep)
            .next(tagPostCountStep)
            .next(categoryPostCountStep)
            .next(userStatisticRedisStep)
            .build();
    }

    @Bean
    public Step summaryStatisticStep() {
        return new StepBuilder("summaryStatisticStep", jobRepository)
            .<Users, List<UserStatistic>>chunk(userStatisticChunkSize, transactionManager)
            .reader(userListReader.notDeletedUserListReader())
            .processor(statisticProcessor.postCountSummaryProcessor())
            .writer(statisticWriter.userStatisticWriter())
            .build();
    }

    @Bean
    public Step dailyPostCountStep(
        ItemProcessor<Users, List<UserStatistic>> dailyPostCountProcessor
    ) {
        return new StepBuilder("dailyPostCountStep", jobRepository)
            .<Users, List<UserStatistic>>chunk(userStatisticChunkSize, transactionManager)
            .reader(userListReader.notDeletedUserListReader())
            .processor(dailyPostCountProcessor)
            .writer(statisticWriter.userStatisticWriter())
            .build();
    }

    @Bean
    public Step tagPostCountStep() {
        return new StepBuilder("tagPostCountStep", jobRepository)
            .<Users, List<UserStatistic>>chunk(userStatisticChunkSize, transactionManager)
            .reader(userListReader.notDeletedUserListReader())
            .processor(statisticProcessor.tagPostCountProcessor())
            .writer(statisticWriter.userStatisticWriter())
            .build();
    }

    @Bean
    public Step categoryPostCountStep() {
        return new StepBuilder("categoryPostCountStep", jobRepository)
            .<Users, List<UserStatistic>>chunk(userStatisticChunkSize, transactionManager)
            .reader(userListReader.notDeletedUserListReader())
            .processor(statisticProcessor.categoryPostCountProcessor())
            .writer(statisticWriter.userStatisticWriter())
            .build();
    }

    @Bean
    public Step userStatisticRedisStep() {
        return new StepBuilder("userStatisticRedisStep", jobRepository)
            .<Users, RedisDto>chunk(100, transactionManager)
            .reader(userListReader.notDeletedUserListReader()) // DB에서 유저조회
            .processor(statisticProcessor.redisStatisticProcessor())
            .writer(statisticWriter.redisWriter())
            .build();
    }

}
