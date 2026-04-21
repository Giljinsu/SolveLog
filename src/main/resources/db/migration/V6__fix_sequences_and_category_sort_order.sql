-- V6__fix_sequences_and_category_sort_order.sql

-- 1. 모든 PK 시퀀스를 현재 테이블 최대값 기준으로 재동기화
-- nextval()이 다음 값부터 정상 발급되도록 max(id), true 방식 사용

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

