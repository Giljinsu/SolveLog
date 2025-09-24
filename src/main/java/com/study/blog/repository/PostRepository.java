package com.study.blog.repository;

import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.entity.Post;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // 임시 게시글 목록
    @Query("select p from Post p where p.isTemp = true and p.user.id = :userId order by p.createdDate")
    List<Post> findByUserId(@Param("userId") Long userId);

    @Query("select count(p.id) from Post p where p.user.username = :username and p.isTemp is false ")
    Long getPostCountByUsername(@Param("username") String username);


//    @Query("select p from Post p where p.user.username = :username and p.isTemp is true")
//    Slice<Post> findByUsernameAndIsTemp(@Param("username") String username, Pageable pageable);
}

