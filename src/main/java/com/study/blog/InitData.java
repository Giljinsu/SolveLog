package com.study.blog;

import com.study.blog.entity.AlarmType;
import com.study.blog.entity.Category;
import com.study.blog.entity.Comment;
import com.study.blog.entity.Post;
import com.study.blog.entity.PostTag;
import com.study.blog.entity.Tag;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.AlarmTypeEnum;
import com.study.blog.entity.enums.Role;
import com.study.blog.repository.AlarmTypeRepository;
import com.study.blog.repository.CategoryRepository;
import com.study.blog.repository.CommentRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.PostTagRepository;
import com.study.blog.repository.TagRepository;
import com.study.blog.repository.UsersRepository;
import com.study.blog.service.PostService;
import com.study.blog.service.PostTagService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitData {
    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final AlarmTypeRepository alarmTypeRepository;
    private final TagRepository tagRepository;
    private final PostTagService postTagService;
    private final PostTagRepository postTagRepository;
//    private final PostService postService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        //given

        //alarm
        AlarmType alarmTypeComment = AlarmType.createAlarmType(AlarmTypeEnum.COMMENT, "{{sender}}님이 '{{postTitle}}'에 댓글을 남겼습니다.");
        AlarmType alarmTypeLike = AlarmType.createAlarmType(AlarmTypeEnum.LIKE, "{{sender}}님이 {{postTitle}}에 좋아요를 눌렀습니다."); //~님 title에 좋아요를 눌렀습니다
        AlarmType alarmTypeReply = AlarmType.createAlarmType(AlarmTypeEnum.REPLY, "{{sender}}님이 회원님의 댓글에 답글을 남겼습니다.");

        alarmTypeRepository.save(alarmTypeComment);
        alarmTypeRepository.save(alarmTypeReply);
        alarmTypeRepository.save(alarmTypeLike);

        //category
        Category parentCategory = Category.createCategory("SEARCH_CATEGORY");
        Category solveCategory = Category.createCategory("문제풀이",parentCategory);
        Category freeCategory = Category.createCategory("자유게시판", parentCategory);

        categoryRepository.save(parentCategory);
        categoryRepository.save(solveCategory);
        categoryRepository.save(freeCategory);



//        tagRepository.save(tag2);




        //user & post
        Users user1 = Users.createUser("user1", "pas", "user1", Role.USER);
        Users user2 = Users.createUser("user2", "pas", "user2", Role.USER);
        usersRepository.save(user1);
        usersRepository.save(user2);

        // 페이지네이션 테스트용 데이터
        for (int i = 0; i < 100; i++) {
            Post post1 = Post.createPost("test"+i,
                "<h1>코딩일기 or SolveLog</h1>\n"
                    + "<h2>작은 블로그 플랫폼 (Mini Medium)\n"
                    + "   글 작성 / 수정 / 삭제 (마크다운 지원) </h2>\n"
                    + "\n"
                    + "태그 기능\n"
                    + "- 유저별 글 관리\n"
                    + "- 좋아요 / 조회수\n"
                    + "- 카테고리 (자유/ 코딩 문제풀이 / 질문) 즉 카테고리 추가시 알아서 추가\n"
                    + "- 관리자 / 유저 역할 추가\n"
                    + "- 댓글 기능\n"
                    + "- 알림 기능 -> 댓글 알림\n"
                    + "- 정렬(조회순 좋아요순 최신순)\n"
                    + "- 페이지네이션 (슬라이스 아닌)\n"
                    + "\n"
                    + "실습 포인트:\n"
                    + "- JPA 양방향 연관관계 (USERS ↔ Post, Post ↔ Comment)\n"
                    + "- React에서 react-markdown 사용\n"
                    + "- 게시글 리스트 무한스크롤 or 페이지네이션\n"
                    + "- 스프링 시큐리티 사용해보기 -> 로그인\n"
                    + "\n"
                    + "미완 기능\n"
                    + "- 로그인 (완?)\n"
                    + "- 조회수 (미완)\n"
                    + "- 로그 기능 (로그인 실패시 로그 남기기)\n"
                    + "- JWT 확장 -> 지금은 session 으로 하는중 (완)\n"
                    + "- 권한에 따른 기능\n"
                    + "- Redis 사용 -> JWT 토큰을 보관용\n"
                    + "- 파일 용량 제한\n"
                    + "- 프론트 리프레시 토큰 쿠키저장\n"
                    + "\n"
                    + "문제\n"
                    + "- 좋아요순 설정 시 설정 안됨\n"
                    + "\n"
                    + "블로그 디자인 참고\n"
                    + "- https://velog.io/\n"
                    + "\n"
                    + "```mermaid\n"
                    + "---\n"
                    + "title: BlogEntity\n"
                    + "---\n"
                    + "erDiagram\n"
                    + "    \n"
                    + "    USERS ||--o{ POST : has\n"
                    + "    USERS ||--o{ COMMENT : has\n"
                    + "    USERS ||--o{ LIKES : has\n"
                    + "    USERS {\n"
                    + "        LONG USER_ID PK\n"
                    + "        STRING USERNAME \"유저 아이디\"\n"
                    + "        STRING PASSWORD \"비밀번호\"\n"
                    + "        STRING NICKNAME \"닉네임\"\n"
                    + "        STRING ROLE \"권한\"\n"
                    + "        LOCALDATETIME CREATED_DATE \"생성일자\"\n"
                    + "    }\n"
                    + "    \n"
                    + "    POST ||--o{ COMMENT : has\n"
                    + "    POST ||--o{ LIKES :has\n"
                    + "    POST ||--o{ CATEGORY : has\n"
                    + "    POST {\n"
                    + "        LONG POST_ID PK\n"
                    + "        LONG USER_ID FK\n"
                    + "        LONG CATEGORY_ID FK\n"
                    + "        STRING TITLE \"제목\"\n"
                    + "        STRING CONTENT \"내용\"\n"
                    + "        INT VIEW_COUNT \"조회수\"\n"
                    + "        LOCALDATETIME CREATED_DATE \"생성일자\"\n"
                    + "        LOCALDATETIME LAST_MODIFIED_DATE \"수정일자\"\n"
                    + "    }\n"
                    + "    \n"
                    + "    COMMENT {\n"
                    + "        LONG COMMENT_ID PK\n"
                    + "        LONG POST_ID FK\n"
                    + "        LONG USER_ID FK\n"
                    + "        STRING COMMENT \"댓글 내용\"\n"
                    + "        LOCALDATETIME CREATED_DATE \"생성일자\"\n"
                    + "        LOCALDATETIME LAST_MODIFIED_DATE \"수정일자\"\n"
                    + "    }\n"
                    + "    \n"
                    + "    LIKES {\n"
                    + "        LONG LIKE_ID PK\n"
                    + "        LONG POST_ID FK\n"
                    + "        LONG USER_ID FK \n"
                    + "    }\n"
                    + "    \n"
                    + "    \"LIKES 테이블 POST_ID + USERS_ID 유니크 제약필요\"\n"
                    + "    \n"
                    + "    FILE {\n"
                    + "        LONG FILE_ID PK\n"
                    + "        LONG POST_ID \n"
                    + "        STRING PATH \"경로\"\n"
                    + "        STRING TYPE \"타입\"\n"
                    + "        STRING ORIGINAL_FILENAME \"파일이름\"\n"
                    + "        LONG SIZE \"크기\"\n"
                    + "        LOCALDATETIME UPLOAD_DATE \"업로드 일자\"\n"
                    + "    }\n"
                    + "    \n"
                    + "    CATEGORY ||--o| CATEGORY : has\n"
                    + "    CATEGORY {\n"
                    + "        LONG CATEGORY_ID PK\n"
                    + "        LONG PARENT_CATEGORY_ID FK \"자기참조 - 부모 카테고리\"\n"
                    + "        STRING TYPE \"타입\"\n"
                    + "    }\n"
                    + "\n"
                    + "```",
                user1, solveCategory, "테스트,테스트2", "요약함", false);

            Comment createComment1 = Comment.createComment("test1", user1, post1);
            Comment createComment2 = Comment.createComment("test2", user2, post1);

            //태그
            Tag tag1 = Tag.createTag("태그"+i);

            tagRepository.save(tag1);


            PostTag postTag1 = PostTag.createPostTag(post1, tag1);
//            PostTag postTag2 = PostTag.createPostTag(post1, tag2);

            postRepository.save(post1);
            commentRepository.save(createComment1);
            commentRepository.save(createComment2);
            postTagRepository.save(postTag1);


        }


    }

}
