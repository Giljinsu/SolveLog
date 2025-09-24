package com.study.blog.service;

import com.study.blog.dto.likes.LikesRequestDto;
import com.study.blog.entity.Likes;
import com.study.blog.entity.Post;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.AlarmTypeEnum;
import com.study.blog.exception.DuplicateLikeException;
import com.study.blog.repository.LikesRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.UsersRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikesRepository likesRepository;
    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final AlarmService alarmService;

    //좋아요 여부
    @Transactional(readOnly = true)
    public boolean isLiked(LikesRequestDto requestDto) {
        Users users = usersRepository.findUsersByUsername(requestDto.getUsername()).orElseThrow();
        return likesRepository.existsByUser_IdAndPost_Id(users.getId(), requestDto.getPostId());
    }

    // 게시글 좋아요 갯수
    @Transactional(readOnly = true)
    public Integer getLikesCount(Long postId) {
        return likesRepository.countByPost_Id(postId);
    }


    // 좋아요
    public Long createLike(LikesRequestDto requestDto) {
        Boolean chkDuplicate = likesRepository.chkDuplicate(requestDto.getUserId(), requestDto.getPostId());

        if (chkDuplicate) {
            throw new DuplicateLikeException();
        }

        Post findPost = postRepository.findById(requestDto.getPostId()).orElseThrow();
        Users findUser = usersRepository.findUsersByUsername(requestDto.getUsername()).orElseThrow();


        Likes newLike = new Likes(findUser, findPost);

        likesRepository.save(newLike);

        //알림생성
        // ~님 title에 좋아요를 눌렀습니다
        if (findPost.getUser() != findUser) {
            Map<String, String> meta = Map.of(
                "sender", findUser.getNickname(),
                "postTitle", findPost.getTitle(),
                "postId", findPost.getId().toString()
                );

            alarmService.createAlarm(findPost.getUser().getUsername(), AlarmTypeEnum.LIKE, meta);

        }

        return newLike.getId();
    }

    // 좋아요 취소
    public void deleteLike(LikesRequestDto requestDto) {
        Users users = usersRepository.findUsersByUsername(requestDto.getUsername()).orElseThrow();
        likesRepository.deleteByUser_IdAndPost_Id(users.getId(), requestDto.getPostId());
//        likesRepository.deleteById(likeId);
    }
}
