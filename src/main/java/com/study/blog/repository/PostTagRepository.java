package com.study.blog.repository;

import com.study.blog.dto.postTag.PostTagResponseDto;
import com.study.blog.dto.postTag.TagCountDto;
import com.study.blog.dto.tag.TagResponseDto;
import com.study.blog.entity.PostTag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @Query("select p.post.id, t.name from PostTag p join p.tag t where p.post.id in :postIds")
    Map<Long, List<String>> findTagNamesAllByPost_Ids(@Param("postIds") List<Long> postIds);

    @Modifying
    @Query("delete from PostTag p where p.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    @Query("select new com.study.blog.dto.tag.TagResponseDto(p.tag.id, p.tag.name) from PostTag p where p.post.id = :postId")
     Optional<List<TagResponseDto>> findByPostId(@Param("postId") Long postId);

    @Query("select new com.study.blog.dto.tag.TagResponseDto(p.tag.id, p.post.id, p.tag.name) from PostTag p where p.post.id in :postIds")
    List<TagResponseDto> findByPostIdSet(@Param("postIds") Collection<Long> postIds);

//    select t.id, t.name, count(pt.tag_id),
//    (select count(post_id) from post where user_id = u.user_id) as allCount
//    from post_tag  pt
//    left outer join tag t on pt.tag_id = t.tag_id
//    left outer join post p on pt.post_id = p.post_id
//    left outer join users u on p.user_id = u.user_id
//    where u.username = 'user1'
//    group by t.tag_id, t.name
    // 유저별 태그 게시물 갯수

    @Query("select new com.study.blog.dto.postTag.TagCountDto(t.id, t.name, count(pt.tag.id)) "
        + "from PostTag pt "
        + "join pt.tag t "
        + "join pt.post p "
        + "join p.user u "
        + "where u.username = :username "
        + "and p.isTemp is false "
        + "group by t.id, t.name "
        + "order by t.name asc"
//        + "order by count(pt.tag.id) desc"
    )
    List<TagCountDto> findTagCountByUsername(@Param("username") String username);

    @Query("select new com.study.blog.dto.postTag.TagCountDto(t.id, t.name, count(pt.tag.id)) "
        + "from PostTag pt "
        + "join pt.tag t "
        + "join pt.post p "
        + "join Likes l on l.post.id = pt.post.id "
        + "join l.user u "
        + "where u.username = :username "
        + "and p.isTemp is false "
        + "group by t.id, t.name "
        + "order by t.name asc"
//        + "order by count(pt.tag.id)"
    )
    List<TagCountDto> findLikesTagCountByUsername(@Param("username") String username);



}
