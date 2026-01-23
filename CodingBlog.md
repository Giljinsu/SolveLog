
<h1>코딩일기 or SolveLog</h1>
<h2>작은 블로그 플랫폼 (Mini Medium)
   글 작성 / 수정 / 삭제 (마크다운 지원) </h2>

태그 기능
- 유저별 글 관리
- 좋아요 / 조회수
- 카테고리 (자유/ 코딩 문제풀이 / 질문) 즉 카테고리 추가시 알아서 추가
- 관리자 / 유저 역할 추가
- 댓글 기능
- 알림 기능 -> 댓글 알림
- 정렬(조회순 좋아요순 최신순)
- 페이지네이션 (슬라이스 아닌)npm

실습 포인트:
- JPA 양방향 연관관계 (USERS ↔ Post, Post ↔ Comment)
- React에서 react-markdown 사용
- 게시글 리스트 무한스크롤 or 페이지네이션
- 스프링 시큐리티 사용해보기 -> 로그인

미완 기능
- 로그인 (완?)
- 조회수 (완)
- 로그 기능 (로그인 실패시 로그 남기기)
- JWT 확장 -> 지금은 session 으로 하는중 (완)
- 권한에 따른 기능
- Redis 사용 -> JWT 리프레시 토큰을 보관용 (완)
- 파일 용량 제한
- 프론트 리프레시 토큰 쿠키저장 (완)
- 대댓글 ( 타깃 부분은 아직) (완)
- 알림기능 (완)
- 요약 및 썸네일 (완)
- 페이지네이션 -> 슬라이드 형식 (완)
- 마이페이지 (완)
- 사이드 메뉴 해당 위치일때 css 처리(완)
- 토큰 만료시 메인화면으로 이동
- 임시저장 로직 점검 필요 (완)
- \# 태그 검색 (완)
- 에러로그 처리 
- 입력값 길이 제한 (완)

- 처음 도커-컴포즈 실행때 flyaway V2(insert)문 작동 안됨 (완)
- CI/CD 구축 (완)
- 이미지 S3 서버 (완)
- certbot 자동 갱신 (완)


문제
- 좋아요순 설정 시 설정 안됨 (완)
- 유저 이미지 설정후 글 작성하면 문제 발생 (완)
  - 아마 글작성 로직중 불필요한 이미지 삭제가 
    있는데 그걸로 인해 유저 이미지 삭제 된듯
- 댓글 대댓글 작성시 해당 댓글 맨위로 올라가는 현상


블로그 디자인 참고
- https://velog.io/

```mermaid
---
title: BlogEntity
---
erDiagram

  USERS ||--o{ POST : writes
  USERS ||--o{ COMMENT : writes
  USERS ||--o{ LIKES : likes
  USERS ||--o{ ALARM : receives

  USERS {
    LONG USER_ID PK
    STRING USERNAME "유저 아이디 (unique)"
    STRING PASSWORD "비밀번호"
    STRING NICKNAME "닉네임"
    STRING ROLE "권한"
    CHARACTER IS_DELETED "삭제여부"
    LOCALDATETIME CREATED_DATE "생성일자"
    LONG USER_IMG_ID "유저 이미지 (File 논리참조)"
    STRING BIO "자기소개"
    LOCALDATETIME DELETED_DATE "삭제일자"
  }

  POST }o--|| CATEGORY : belongs_to
  POST ||--o{ COMMENT : has
  POST ||--o{ LIKES : has
  POST ||--o{ POST_TAG : has

  POST {
    LONG POST_ID PK
    LONG USER_ID FK
    LONG CATEGORY_ID FK
    STRING TITLE "제목"
    STRING CONTENT "내용"
    INT VIEW_COUNT "조회수"
    STRING TAGS "태그 문자열"
    STRING SUMMARY "요약"
    BOOLEAN IS_TEMP "임시저장여부"
    LOCALDATETIME CREATED_DATE "생성일자"
    LOCALDATETIME LAST_MODIFIED_DATE "수정일자"
  }

  COMMENT ||--o{ COMMENT : has_child
  COMMENT {
    LONG COMMENT_ID PK
    LONG POST_ID FK
    LONG USER_ID FK
    LONG PARENT_COMMENT_ID FK "부모 댓글"
    STRING COMMENT "댓글 내용"
    LOCALDATETIME CREATED_DATE "생성일자"
    LOCALDATETIME LAST_MODIFIED_DATE "수정일자"
  }

  LIKES {
    LONG LIKE_ID PK
    LONG POST_ID FK
    LONG USER_ID FK
  }

  "LIKES 테이블 (POST_ID, USER_ID) UNIQUE 제약"

  FILE {
    LONG FILE_ID PK
    LONG POST_ID "논리 FK (연관관계 미설정)"
    STRING USERNAME "업로드 사용자"
    STRING PATH "경로"
    STRING TYPE "ENUM(FileType)"
    STRING ORIGINAL_FILENAME "파일 이름"
    LONG SIZE "파일 크기"
    BOOLEAN IS_THUMBNAIL "썸네일 여부"
    BOOLEAN IS_USER_IMG "유저 이미지 여부"
    LOCALDATETIME UPLOAD_DATE "업로드 일자"
  }

  CATEGORY ||--o{ CATEGORY : has_child
  CATEGORY {
    LONG CATEGORY_ID PK
    LONG PARENT_CATEGORY_ID FK "부모 카테고리"
    STRING TYPE "카테고리 타입 (unique)"
  }

  ALARM }o--|| ALARM_TYPE : typed_as
  ALARM {
    LONG ALARM_ID PK
    LONG USER_ID FK
    LONG ALARM_TYPE_ID FK
    STRING METADATA "JSON 메타데이터"
    BOOLEAN IS_VIEWED "알림 조회 여부"
  }

  ALARM_TYPE {
    LONG ALARM_TYPE_ID PK
    STRING TYPE "알람 타입 (enum, unique)"
    STRING TEMPLATE "알람 메시지 템플릿"
  }

  TAG ||--o{ POST_TAG : used_by
  TAG {
    LONG TAG_ID PK
    STRING NAME "태그 이름 (unique)"
  }

  POST_TAG {
    LONG POST_TAG_ID PK
    LONG POST_ID FK
    LONG TAG_ID FK
  }


```

