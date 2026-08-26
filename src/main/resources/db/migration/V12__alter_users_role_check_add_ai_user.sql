-- AI_USER Role 추가에 따른 users.role CHECK 제약 갱신
-- 기존 USER / ADMIN 값과 데이터는 그대로 유지된다.

ALTER TABLE users
    DROP CONSTRAINT users_role_check;

ALTER TABLE users
    ADD CONSTRAINT users_role_check
        CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'AI_USER'::character varying, 'ADMIN'::character varying])::text[])));
