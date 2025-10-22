-- category 필수 데이터 삽입
INSERT INTO category (id, parent_category_id, type)
VALUES (1, NULL, 'SEARCH_CATEGORY')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, parent_category_id, type)
VALUES (2, 1, '문제풀이')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, parent_category_id, type)
VALUES (3, 1, '자유게시판')
    ON CONFLICT (id) DO NOTHING;


-- alarm_type의 필수 템플릿 삽입
INSERT INTO alarm_type (alarm_type_id, template, type)
VALUES (1, '{{sender}}님이 ''{{postTitle}}''에 댓글을 남겼습니다.', 'COMMENT')
    ON CONFLICT (alarm_type_id) DO NOTHING;

INSERT INTO alarm_type (alarm_type_id, template, type)
VALUES (2, '{{sender}}님이 {{postTitle}}에 좋아요를 눌렀습니다.', 'LIKE')
    ON CONFLICT (alarm_type_id) DO NOTHING;

INSERT INTO alarm_type (alarm_type_id, template, type)
VALUES (3, '{{sender}}님이 회원님의 댓글에 답글을 남겼습니다.', 'REPLY')
    ON CONFLICT (alarm_type_id) DO NOTHING;