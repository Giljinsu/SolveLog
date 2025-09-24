package com.study.blog.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.dto.post.SearchCondition;
import com.study.blog.dto.post.SearchOrderType;
import com.study.blog.dto.post.SearchType;
import com.study.blog.entity.Category;
import com.study.blog.entity.Comment;
import com.study.blog.entity.Post;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.Role;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PostRepositoryCustomImplTest {
    @Autowired UsersRepository usersRepository;
    @Autowired PostRepository postRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("검색 조건 테스트")
    @Transactional
    public void searchTest() throws Exception {
        Category category = Category.createCategory("category");
        categoryRepository.save(category);

        Users user1 = Users.createUser("user1", "pas", "user1", Role.USER);
        Post post1 = Post.createPost("test", "content", user1, category, "test", "요약",false);
        Post post2 = Post.createPost("test2", "content2", user1, category, "test", "요약", false);
        post1.setViews(10);
        post2.setViews(20);

        usersRepository.save(user1);
        postRepository.save(post1);
        postRepository.save(post2);

        em.flush();
        em.clear();

        SearchCondition searchCondition1 = new SearchCondition("test", SearchType.TITLE, SearchOrderType.VIEWS);
        SearchCondition searchCondition2 = new SearchCondition("CONTENT", SearchType.CONTENT, SearchOrderType.LIKE);
        SearchCondition searchCondition3 = new SearchCondition("CONTENT", SearchType.TITLE_AND_CONTENT, SearchOrderType.LIKE);
        SearchCondition searchCondition4 = new SearchCondition("user", SearchType.USERNAME, SearchOrderType.LATEST);

        PageRequest pageRequest = PageRequest.of(0, 2);

        Slice<PostResponseDto> findPostList1 = postRepository.findListWithSearchCondition(
            searchCondition1, pageRequest);

        Slice<PostResponseDto> findPostList2 = postRepository.findListWithSearchCondition(
            searchCondition2, pageRequest);

        Slice<PostResponseDto> findPostList3 = postRepository.findListWithSearchCondition(
            searchCondition3, pageRequest);

        Slice<PostResponseDto> findPostList4 = postRepository.findListWithSearchCondition(
            searchCondition4, pageRequest);

        assertThat(findPostList1.getSize()).isEqualTo(2);
        assertThat(findPostList2.getSize()).isEqualTo(2);


        assertThat(findPostList1.getContent().getFirst()).extracting("title").isEqualTo("test2");// 조회수

        assertThat(findPostList3.getSize()).isEqualTo(2);

        assertThat(findPostList4.getSize()).isEqualTo(2);


    }

    @Test
    @Transactional
    public void getCommentByPostId() throws Exception {
        Category category = Category.createCategory("category");
        categoryRepository.save(category);

        Users user1 = Users.createUser("user1", "pas", "user1", Role.USER);
        Post post1 = Post.createPost("test", "content", user1, category, "test", "요약", false);

        Comment createComment = Comment.createComment("test1", user1, post1);

        usersRepository.save(user1);
        postRepository.save(post1);
        commentRepository.save(createComment);

        em.flush();
        em.clear();

        List<Comment> comments = commentRepository.findCommentByPostId(post1.getId());

        //when
        assertThat(comments.size()).isEqualTo(1);

        //then
    }

    @Test
    @Transactional
    public void getChildCommentTest() {
        Category category = Category.createCategory("category");
        categoryRepository.save(category);

        Users user1 = Users.createUser("user1", "pas", "user1", Role.USER);
        Post post1 = Post.createPost("test", "content", user1, category, "test", "요약",false);
        Post post2 = Post.createPost("test2", "content2", user1, category, "test", "요약", false);
        post1.setViews(10);
        post2.setViews(20);

        Comment createComment1 = Comment.createComment("test1", user1, post1);

        usersRepository.save(user1);
        postRepository.save(post1);
        postRepository.save(post2);
        commentRepository.save(createComment1);

        Comment createComment2 = Comment.createComment("test2", user1, post1, createComment1);
        Comment createComment3 = Comment.createComment("test3", user1, post1, createComment1);

        commentRepository.save(createComment2);
        commentRepository.save(createComment3);
//        assertThat(createComment1.getChildComments().size()).isEqualTo(2);
        em.flush();
        em.clear();

        Comment findComment = commentRepository.getReferenceById(createComment1.getId());

        assertThat(findComment.getChildComments().size()).isEqualTo(2);

    }
}