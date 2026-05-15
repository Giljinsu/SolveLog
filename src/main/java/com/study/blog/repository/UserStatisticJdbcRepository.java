package com.study.blog.repository;

import com.study.blog.entity.UserStatistic;
import java.sql.Date;
import java.sql.Types;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserStatisticJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkUpsert(List<UserStatistic> statistics) {
        jdbcTemplate.batchUpdate(
            """
            INSERT INTO user_statistic
            (
                user_id,
                statistic_type,
                category_name,
                tag_name,
                statistic_count,
                statistic_date,
                created_date,
                modified_date
            )
            VALUES (?, ?, ?, ?, ?, ?, now(), now())
            ON CONFLICT (
                user_id,
                statistic_type,
                COALESCE(category_name, ''),
                COALESCE(tag_name, ''),
                COALESCE(statistic_date, TIMESTAMP '0001-01-01 00:00:00')
            )
            DO UPDATE SET
                statistic_count = EXCLUDED.statistic_count,
                modified_date = now()
            """,
            statistics,
            1000,
            (ps, statistic) -> {
                ps.setLong(1, statistic.getUser().getId());
                ps.setString(2, statistic.getStatisticType().name());
                ps.setString(3, statistic.getCategoryName());
                ps.setString(4, statistic.getTagName());
                ps.setLong(5, statistic.getStatisticCount());

                if (statistic.getStatisticDate() == null) {
                    ps.setNull(6, Types.DATE);
                } else {
                    ps.setDate(6, Date.valueOf(statistic.getStatisticDate()));
                }
            }
        );
    }
}