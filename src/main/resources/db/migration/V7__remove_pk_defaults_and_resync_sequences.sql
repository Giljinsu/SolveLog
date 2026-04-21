-- V7__remove_pk_defaults_and_resync_sequences.sql
-- V5, V6는 다 무용지물

-- 1. V5에서 추가한 PK 컬럼 DEFAULT nextval(...) 제거
ALTER TABLE public.alarm
    ALTER COLUMN alarm_id DROP DEFAULT;

ALTER TABLE public.alarm_type
    ALTER COLUMN alarm_type_id DROP DEFAULT;

ALTER TABLE public.category
    ALTER COLUMN id DROP DEFAULT;

ALTER TABLE public.comment
    ALTER COLUMN comment_id DROP DEFAULT;

ALTER TABLE public.file
    ALTER COLUMN id DROP DEFAULT;

ALTER TABLE public.likes
    ALTER COLUMN like_id DROP DEFAULT;

ALTER TABLE public.post
    ALTER COLUMN post_id DROP DEFAULT;

ALTER TABLE public.post_tag
    ALTER COLUMN post_tag_id DROP DEFAULT;

ALTER TABLE public.tag
    ALTER COLUMN tag_id DROP DEFAULT;

ALTER TABLE public.users
    ALTER COLUMN user_id DROP DEFAULT;

-- 2. Hibernate/@GeneratedValue가 사용할 수 있는 sequence 값을
-- 현재 테이블 최대 PK 기준으로 재동기화
SELECT setval('public.alarm_seq',
              COALESCE((SELECT MAX(alarm_id) FROM public.alarm), 0),
              true);

SELECT setval('public.alarm_type_seq',
              COALESCE((SELECT MAX(alarm_type_id) FROM public.alarm_type), 0),
              true);

SELECT setval('public.category_seq',
              COALESCE((SELECT MAX(id) FROM public.category), 0),
              true);

SELECT setval('public.comment_seq',
              COALESCE((SELECT MAX(comment_id) FROM public.comment), 0),
              true);

SELECT setval('public.file_seq',
              COALESCE((SELECT MAX(id) FROM public.file), 0),
              true);

SELECT setval('public.likes_seq',
              COALESCE((SELECT MAX(like_id) FROM public.likes), 0),
              true);

SELECT setval('public.post_seq',
              COALESCE((SELECT MAX(post_id) FROM public.post), 0),
              true);

SELECT setval('public.post_tag_seq',
              COALESCE((SELECT MAX(post_tag_id) FROM public.post_tag), 0),
              true);

SELECT setval('public.tag_seq',
              COALESCE((SELECT MAX(tag_id) FROM public.tag), 0),
              true);

SELECT setval('public.users_seq',
              COALESCE((SELECT MAX(user_id) FROM public.users), 0),
              true);