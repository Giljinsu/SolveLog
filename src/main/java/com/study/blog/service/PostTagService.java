package com.study.blog.service;

import com.study.blog.dto.postTag.PostTagResponseDto;
import com.study.blog.dto.postTag.TagCountDto;
import com.study.blog.repository.LikesRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.PostTagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostTagService {
    private final PostTagRepository postTagRepository;
    private final PostRepository postRepository;
    private final LikesRepository likesRepository;

    // 유저이름별 태그 게시글 수
    public PostTagResponseDto getPostCountPerTagByUsername(String username) {
        List<TagCountDto> tagCountByUsername = postTagRepository.findTagCountByUsername(username);
        Long totalPostCount = postRepository.getPostCountByUsername(username); // 유저의 전체 게시글 수
        return new PostTagResponseDto(tagCountByUsername, totalPostCount);
    }

    // 유저이름별 좋아요한 태그 게시글 수
    public PostTagResponseDto getLikePostCountPerTagByUsername(String username) {
        List<TagCountDto> tagCountByUsername = postTagRepository.findLikesTagCountByUsername(username);
        Long totalPostCount = likesRepository.getLikesCountByUsername(username); // 유저의 전체 게시글 수
        return new PostTagResponseDto(tagCountByUsername, totalPostCount);
    }


}
