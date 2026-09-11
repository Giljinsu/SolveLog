-- SolveLog 통계 API 부하테스트용 시드 데이터
-- 기존 개발 데이터는 절대 건드리지 않는다: 모든 신규 유저는 username 'loadtest_user_%' 접두사,
-- 로그인 전용 계정은 'loadtest_runner' 로 명확히 구분한다.
-- ID는 전부 각 테이블의 시퀀스 nextval() 로 채번하므로 기존 데이터와 충돌하지 않는다.

BEGIN;

-- 0) 로그인 전용 계정 (실제 부하 대상은 아니고, JWT 쿠키 발급용)
--    비밀번호 해시는 bcrypt('loadtest1234!')
INSERT INTO users (user_id, nickname, password, role, username, is_deleted, created_date)
VALUES (
    nextval('users_seq'),
    'loadtest_runner',
    '$2b$10$ormOgMzLQ5Jx/yi7abllEuOyEJzextsF7meeGlEP6EGbEkV/PDxsS',
    'USER',
    'loadtest_runner',
    'n',
    now()
);

-- 1) 1,000명 유저 생성 (라이트 700 / 미디엄 250 / 헤비 50 분포)
CREATE TEMP TABLE loadtest_seed_users AS
SELECT
    n AS seq_no,
    nextval('users_seq') AS user_id,
    'loadtest_user_' || lpad(n::text, 4, '0') AS username,
    CASE
        WHEN n <= 700 THEN 'light'
        WHEN n <= 950 THEN 'medium'
        ELSE 'heavy'
    END AS tier,
    CASE
        WHEN n <= 700 THEN 5 + floor(random() * 36)::int          -- 5~40
        WHEN n <= 950 THEN 100 + floor(random() * 201)::int       -- 100~300
        ELSE 500 + floor(random() * 401)::int                     -- 500~900
    END AS post_count
FROM generate_series(1, 1000) AS n;

INSERT INTO users (user_id, nickname, password, role, username, is_deleted, created_date)
SELECT
    user_id,
    username,           -- nickname은 username과 동일하게 (유니크 제약 없음)
    '$2b$10$ormOgMzLQ5Jx/yi7abllEuOyEJzextsF7meeGlEP6EGbEkV/PDxsS',
    'USER',
    username,
    'n',
    now() - (random() * interval '700 days')
FROM loadtest_seed_users;

-- 2) 게시글 생성: 유저별 post_count 만큼, 카테고리/작성일 랜덤 분산
--    category_id: 2=문제풀이, 3=자유게시판, 4=학습기록 (기존 데이터 그대로 사용)
CREATE TEMP TABLE loadtest_seed_posts AS
SELECT
    nextval('post_seq') AS post_id,
    u.user_id,
    u.username,
    (ARRAY[2,3,4])[1 + floor(random() * 3)::int] AS category_id,
    (now() - (random() * interval '730 days'))::timestamp AS created_date,
    floor(random() * 500)::int AS view_count
FROM loadtest_seed_users u
CROSS JOIN LATERAL generate_series(1, u.post_count) AS gs(n);

INSERT INTO post (post_id, user_id, category_id, created_date, last_modified_date,
                   is_temp, view_count, title, content, summary)
SELECT
    post_id,
    user_id,
    category_id,
    created_date,
    created_date,
    false,
    view_count,
    'loadtest post ' || post_id,
    'load test content for benchmarking statistic aggregation',
    'loadtest summary'
FROM loadtest_seed_posts;

-- 3) 게시글당 태그 2~3개 (기존 29개 tag 재사용, 신규 tag 생성 안 함)
INSERT INTO post_tag (post_tag_id, post_id, tag_id)
SELECT
    nextval('post_tag_seq'),
    p.post_id,
    t.tag_id
FROM loadtest_seed_posts p
CROSS JOIN LATERAL (
    SELECT tag_id FROM tag ORDER BY random() LIMIT (2 + floor(random() * 2)::int)
) t;

COMMIT;

-- 요약 출력
SELECT 'users' AS tbl, count(*) FROM users WHERE username LIKE 'loadtest_%'
UNION ALL
SELECT 'posts', count(*) FROM post WHERE title LIKE 'loadtest post %'
UNION ALL
SELECT 'post_tag', count(*) FROM post_tag pt JOIN post p ON p.post_id = pt.post_id WHERE p.title LIKE 'loadtest post %';
