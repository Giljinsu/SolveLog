CREATE UNIQUE INDEX uk_user_statistic
ON user_statistic (
               user_id,
               statistic_type,
               COALESCE(category_name, ''),
               COALESCE(tag_name, ''),
               COALESCE(statistic_date, TIMESTAMP '0001-01-01 00:00:00')
);