-- V5__bind_sequences_and_add_category_sort_order.sql

-- 1. 각 PK 컬럼에 시퀀스 연결
ALTER TABLE public.alarm
    ALTER COLUMN alarm_id SET DEFAULT nextval('public.alarm_seq');

ALTER TABLE public.alarm_type
    ALTER COLUMN alarm_type_id SET DEFAULT nextval('public.alarm_type_seq');

ALTER TABLE public.category
    ALTER COLUMN id SET DEFAULT nextval('public.category_seq');

ALTER TABLE public.comment
    ALTER COLUMN comment_id SET DEFAULT nextval('public.comment_seq');

ALTER TABLE public.file
    ALTER COLUMN id SET DEFAULT nextval('public.file_seq');

ALTER TABLE public.likes
    ALTER COLUMN like_id SET DEFAULT nextval('public.likes_seq');

ALTER TABLE public.post
    ALTER COLUMN post_id SET DEFAULT nextval('public.post_seq');

ALTER TABLE public.post_tag
    ALTER COLUMN post_tag_id SET DEFAULT nextval('public.post_tag_seq');

ALTER TABLE public.tag
    ALTER COLUMN tag_id SET DEFAULT nextval('public.tag_seq');

ALTER TABLE public.users
    ALTER COLUMN user_id SET DEFAULT nextval('public.users_seq');

-- 2. 각 시퀀스를 현재 최대 PK 값 기준으로 동기화
SELECT setval('public.alarm_seq', COALESCE((SELECT MAX(alarm_id) FROM public.alarm), 0) + 1, false);
SELECT setval('public.alarm_type_seq', COALESCE((SELECT MAX(alarm_type_id) FROM public.alarm_type), 0) + 1, false);
SELECT setval('public.category_seq', COALESCE((SELECT MAX(id) FROM public.category), 0) + 1, false);
SELECT setval('public.comment_seq', COALESCE((SELECT MAX(comment_id) FROM public.comment), 0) + 1, false);
SELECT setval('public.file_seq', COALESCE((SELECT MAX(id) FROM public.file), 0) + 1, false);
SELECT setval('public.likes_seq', COALESCE((SELECT MAX(like_id) FROM public.likes), 0) + 1, false);
SELECT setval('public.post_seq', COALESCE((SELECT MAX(post_id) FROM public.post), 0) + 1, false);
SELECT setval('public.post_tag_seq', COALESCE((SELECT MAX(post_tag_id) FROM public.post_tag), 0) + 1, false);
SELECT setval('public.tag_seq', COALESCE((SELECT MAX(tag_id) FROM public.tag), 0) + 1, false);
SELECT setval('public.users_seq', COALESCE((SELECT MAX(user_id) FROM public.users), 0) + 1, false);

-- 3. category에 sort_order 추가
ALTER TABLE public.category
    ADD COLUMN sort_order INT;

-- 4. 기존 데이터 반영
UPDATE public.category
SET sort_order = 1
WHERE type = '문제풀이';

UPDATE public.category
SET sort_order = 3
WHERE type = '자유게시판';

INSERT INTO public.category (parent_category_id, type, sort_order)
VALUES (1, '학습기록', 2);