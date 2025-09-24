package com.study.blog.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
        @Value("${spring.data.redis.host}") String host,
        @Value("${spring.data.redis.port}") int port,
        @Value("${spring.data.redis.password:}") String password, // 없으면 빈 문자열
        @Value("${spring.data.redis.ssl:false}") boolean useSsl,
        @Value("${spring.data.redis.timeout:2000}") long timeoutMillis
    ) {
        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            conf.setPassword(RedisPassword.of(password));
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
            LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeoutMillis)); // 응답 시간 제한


        if (useSsl) builder.useSsl(); // 운영 TLS 일 때

        return new LettuceConnectionFactory(conf, builder.build());
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory rcf) {
        // RedisTemplate의 Serialize, Deserialize로 String을 사용하는 StringRedisTemplate
        return new StringRedisTemplate(rcf);
    }

}
