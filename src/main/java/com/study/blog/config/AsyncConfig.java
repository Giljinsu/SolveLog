package com.study.blog.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// Error Monitoring AI 분석 전용 비동기 Executor (analysis.md §11).
// 이 프로젝트에서 @Async를 사용하는 유일한 지점이므로 이름을 명시해 향후 혼선을 막는다.
// GPT 호출이 몰려도 동시 요청 수를 작게 제한해 OpenAI rate limit/비용 폭증을 막는다.
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("errorAnalysisExecutor")
    public Executor errorAnalysisExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("error-analysis-");
        executor.initialize();
        return executor;
    }
}
