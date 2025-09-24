package com.study.blog.service;

import static org.junit.jupiter.api.Assertions.*;

import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.entity.Category;
import com.study.blog.entity.Comment;
import com.study.blog.entity.Post;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.Role;
import com.study.blog.repository.CategoryRepository;
import com.study.blog.repository.CommentRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.UsersRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PostServiceTest {

    @Autowired UsersRepository usersRepository;
    @Autowired PostRepository postRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired EntityManager em;
    @Autowired PostService postService;

    @Test
    @DisplayName("한 개의 포스트를 가져올때 comment 에서 n+1 문제 발생하는지 확인용")
    @Transactional
    public void checkCommentN1() throws Exception {
        //given
        Category category = Category.createCategory("category");
        categoryRepository.save(category);

        Users user1 = Users.createUser("user1", "pas", "user1", Role.USER);
        Users user2 = Users.createUser("user2", "pas", "user1", Role.USER);
        Post post1 = Post.createPost("test", "content", user1, category,"tags", "요약", false);

        Comment createComment1 = Comment.createComment("test1", user1, post1);
        Comment createComment2 = Comment.createComment("test2", user2, post1);

        usersRepository.save(user1);
        usersRepository.save(user2);
        postRepository.save(post1);
        commentRepository.save(createComment1);
        commentRepository.save(createComment2);

        em.flush();
        em.clear();

        PostResponseDto detailPost = postService.getDetailPost(post1.getId());

        Assertions.assertThat(detailPost.getComments().size()).isEqualTo(2);

    }


}