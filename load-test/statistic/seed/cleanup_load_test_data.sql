-- 부하테스트 시드 데이터 원복용 스크립트 (username 'loadtest_%' 패턴 기준으로만 삭제)
-- FK 순서: post_tag -> user_statistic -> post -> users

BEGIN;

DELETE FROM post_tag
WHERE post_id IN (SELECT post_id FROM post WHERE title LIKE 'loadtest post %');

DELETE FROM user_statistic
WHERE user_id IN (SELECT user_id FROM users WHERE username LIKE 'loadtest_%');

DELETE FROM post
WHERE title LIKE 'loadtest post %';

DELETE FROM users
WHERE username LIKE 'loadtest_%';

COMMIT;

SELECT 'remaining loadtest users' AS check, count(*) FROM users WHERE username LIKE 'loadtest_%';
