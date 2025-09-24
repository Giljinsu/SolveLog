package com.study.blog.repository;

import com.study.blog.entity.Likes;
import com.study.blog.entity.Users;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikesRepository extends JpaRepository<Likes, Long> {

    @Query("select count(l.id) > 0 "
        + "from Likes l join l.user u join l.post p "
        + "where u.id = :userId and p.id = :postId")
    Boolean chkDuplicate(@Param("userId") Long userId, @Param("postId") Long postId);

    //좋아요 여부
    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);

    List<Likes> findByUser_Id(Long userId);

    void deleteByUser_IdAndPost_Id(Long userId, Long postId);

    Integer countByPost_Id(Long postId);

    @Query("select count(l.id) from Likes l where l.user.username = :username")
    Long getLikesCountByUsername(@Param("username") String username);
}
