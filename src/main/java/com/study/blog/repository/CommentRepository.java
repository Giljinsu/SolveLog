package com.study.blog.repository;

import com.study.blog.dto.comment.CommentResponseDto;
import com.study.blog.entity.Comment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c "
        + "from Comment c "
        + "left join fetch c.childComments cc "
        + "join fetch c.user "
        + "where c.post.id = :postId "
        + "and c.parentComment is null "
        + "order by c.createdDate")
    List<Comment> findCommentByPostId(@Param("postId") Long postId);


    Integer countCommentsByPost_Id(Long postId);

    @Modifying
    @Query("delete from Comment c where c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long id);

    List<Comment> findCommentById(Long id);

    @Modifying
    @Query("delete from Comment c where c.parentComment.id = :parentCommentId")
    void deleteCommentsByParentCommentId(@Param("parentCommentId") Long parentCommentId);
}
