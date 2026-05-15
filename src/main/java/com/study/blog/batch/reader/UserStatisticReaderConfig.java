package com.study.blog.batch.reader;

import com.study.blog.entity.Users;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UserStatisticReaderConfig {
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaPagingItemReader<Users> notDeletedUserListReader() {
        return new JpaPagingItemReaderBuilder<Users>()
            .name("userListReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString(
                "select u "
                    + "from Users u "
                    + "where u.isDeleted != 'y' "
                    + "order by u.id"
            )
            .pageSize(100)
            .build();
    }
}
