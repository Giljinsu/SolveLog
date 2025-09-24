package com.study.blog.repository;

import com.study.blog.entity.File;
import com.study.blog.entity.Post;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<List<File>> findFileByPostId(Long postId);

    @Modifying // 벌크
    @Query("delete from File f where f.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    Optional<List<File>> findByUsernameAndPostIdIsNull(String username);

    @Query(
        "select f "
        + "from File f "
        + "where f.username = :username "
        + "and f.postId is null "
        + "and f.isUserImg is false "
    )
    Optional<List<File>> findUsingFileByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update File f set f.postId = :postId where f.username = :username and f.postId is null and f.isUserImg is false")
    void bulkUpdatePostIdWherePostIdIsNull(@Param("postId") Long postId, @Param("username") String username);

    // 임시파일 삭제
    void deleteByUsernameAndPostIdIsNull(String username);

    @Query("select f from File f where f.isUserImg is true and f.username = :username")
    Optional<File> findUserImgByUsername(@Param("username") String username);


    @Query("select f from File f where f.isUserImg is true and f.username in :usernames")
    List<File> findUserImgListByUsernames(@Param("usernames") Collection<String> usernames);
}
