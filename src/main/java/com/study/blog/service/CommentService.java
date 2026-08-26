package com.study.blog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.blog.dto.alarm.AlarmRequestDto;
import com.study.blog.dto.comment.CommentRequestDto;
import com.study.blog.dto.comment.CommentResponseDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.entity.Comment;
import com.study.blog.entity.File;
import com.study.blog.entity.Post;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.AlarmTypeEnum;
import com.study.blog.exception.CommentAccessDeniedException;
import com.study.blog.exception.CommentNotFoundException;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.exception.PostNotFoundException;
import com.study.blog.repository.CommentRepository;
import com.study.blog.repository.FileRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.UsersRepository;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FileRepository fileRepository;
    private final AlarmService alarmService;
    private final ObjectMapper objectMapper;

    // 댓글 리스트 조회 (게시글)
    public List<CommentResponseDto> getComments(Long postId) {
        Integer commentCnt = commentRepository.countCommentsByPost_Id(postId);

        List<Comment> comments = commentRepository.findCommentByPostId(postId);



        Map<String, FileResponseDto> userImgMap = getUserImgMap(comments);
//            .stream().collect(Collectors.toMap(File::getUsername, Function.identity()));

        return comments
            .stream()
            .map(comment -> new CommentResponseDto(
                comment.getId(),
                comment.getUser().getNickname(),
                comment.getUser().getUsername(),
                comment.getComment(),
                comment.getCreatedDate(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                comment.getChildComments() != null ?
                    comment.getChildComments().stream()
                        .sorted(Comparator.comparing(Comment::getCreatedDate))
                        .map(childComment -> new CommentResponseDto(
                        childComment.getId(),
                        childComment.getUser().getNickname(),
                        childComment.getUser().getUsername(),
                        childComment.getComment(),
                        childComment.getCreatedDate(),
                        childComment.getParentComment() != null ? childComment.getParentComment()
                            .getId() : null,
                        userImgMap.get(childComment.getUser().getUsername())
                    )).toList() : null,
                userImgMap.get(comment.getUser().getUsername()),
                commentCnt
            ))
            .toList();
    }

    public Map<String, FileResponseDto> getUserImgMap(List<Comment> comments) {
//        Set<String> usernames = comments.stream()
//            .map(comment -> comment.getUser().getUsername())
//            .collect(Collectors.toSet());

        //map()은 스트림의 각 요소를 변환하여 새로운 요소로 매핑하는 중간 연산입니다. 1:1 매핑이라고 생각하면 됩니다.
        //flatMap()은 스트림의 각 요소를 스트림으로 변환한 후, 모든 스트림을 하나의 스트림으로 평면화합니다.
        //1:N 매핑이라고 볼 수 있습니다.
        Set<String> usernames = comments.stream()
            .flatMap(comment -> Stream.concat(
                Stream.of(comment.getUser().getUsername()),
                comment.getChildComments().stream().map(cc -> cc.getUser().getUsername())
            ))
            .collect(Collectors.toSet());

        return fileRepository.findUserImgListByUsernames(usernames)
            .stream().collect(Collectors.toMap(
                File::getUsername,
                file -> new FileResponseDto(file.getId(), file.getOriginalFileName(), file.getPath())
            ));
    }


    //댓글 추가
    public Long createComment(CommentRequestDto commentRequestDto) {
        Users findUser = usersRepository.findUsersByUsername(commentRequestDto.getUsername())
            .orElseThrow(NotExistUserException::new); // 댓글 작성자
        Post findPost = postRepository.findById(commentRequestDto.getPostId())
            .orElseThrow(PostNotFoundException::new); // 댓글 작성한 게시글

        Comment newComment;
        Comment parentComment = null; // 부모 댓글
        if (commentRequestDto.getParentCommentId() != null) { // 대댓글 여부
            parentComment = commentRepository.findById(commentRequestDto.getParentCommentId())
                .orElseThrow(CommentNotFoundException::new);

            newComment = Comment.createComment(commentRequestDto.getComment(), findUser,
                findPost, parentComment);

            commentRepository.save(newComment);


            // 직접 JSON을 만드는것은 위험하다.
//                String metadata = "{ sender : "+findUser.getNickname()+" }";
            Map<String, String> meta = Map.of(
                "sender", findUser.getNickname(),
                "postId", findPost.getId().toString(),
                "postTitle", findPost.getTitle(),
                "targetCommentId", newComment.getId().toString()
            );

            // 대댓글 알림
            // 홍길동님이 회원님의 댓글에 답글을 남겼습니다.
            // 멘션부분
            Users mentionTargetUser = null;
            if (commentRequestDto.getMentionCommentId() != null) {
                Comment findComment = commentRepository.findById(
                    commentRequestDto.getMentionCommentId())
                    .orElseThrow(CommentNotFoundException::new); // 멘션된 댓글

                mentionTargetUser = findComment.getUser(); // 멘션된 유저
                if (!mentionTargetUser.equals(findUser)) {
                    alarmService.createAlarm(mentionTargetUser.getUsername(), AlarmTypeEnum.REPLY, meta);
                }
            }

            // 멘션된 유저와 부모 유저가 다르면 부모와 멘션된 유저에게 알림 보냄
            if (!parentComment.getUser().equals(findUser) && mentionTargetUser != null &&
                !mentionTargetUser.equals(parentComment.getUser())) {
                alarmService.createAlarm(parentComment.getUser().getUsername(), AlarmTypeEnum.REPLY, meta);
            }

        } else {
            newComment = Comment.createComment(commentRequestDto.getComment(), findUser, findPost);
            commentRepository.save(newComment);
        }


//        commentRepository.save(newComment);

        //댓글 알림
        // ~님이 title 에 댓글을 남겼습니다.
        if (!findUser.equals(findPost.getUser())) {
            // 작성자 부모 작성자 같고
            // 직접 JSON을 만드는것은 위험하다.
//            String metadata = "{ "
//                + "sender : "+findUser.getNickname() + ","
//                + "title : "+ findPost.getTitle()
//                + "}";

            // 부모댓글과 게시물 작성자가 같으면 return -> 대댓글 알림만 보냄
            if (parentComment != null && parentComment.getUser().equals(findPost.getUser()))
                return newComment.getId();

            Map<String, String> meta = Map.of(
                "sender", findUser.getNickname(),
                "postTitle", findPost.getTitle(),
                "postId", findPost.getId().toString(),
                "targetCommentId", newComment.getId().toString()
            );
            alarmService.createAlarm(findPost.getUser().getUsername(), AlarmTypeEnum.COMMENT, meta);
        }

        return newComment.getId();
    }


    //수정
    public CommentResponseDto updateComment(CommentRequestDto commentRequestDto) {
        Comment findComment = commentRepository.findById(commentRequestDto.getCommentId())
            .orElseThrow(CommentNotFoundException::new);

        findComment.updateComment(commentRequestDto.getComment());

        // 프로필 이미지를 업로드하지 않은 사용자가 대부분이라 없는 것이 정상이다 - getComments()의
        // userImgMap과 동일하게 없으면 null로 둔다(bare orElseThrow() 리팩터링에서 발견된 버그 수정,
        // 이전에는 프로필 이미지가 없는 사용자의 댓글을 수정할 때마다 500이 발생했다).
        File userImg = fileRepository.findUserImgByUsername(findComment.getUser().getUsername())
            .orElse(null);

        return new CommentResponseDto(
            findComment.getId(),
            findComment.getUser().getNickname(),
            findComment.getUser().getUsername(),
            findComment.getComment(),
            findComment.getCreatedDate(),
            findComment.getParentComment() != null ? findComment.getParentComment().getId() : null,
            userImg != null
                ? new FileResponseDto(userImg.getId(), userImg.getOriginalFileName(), userImg.getPath())
                : null
        );
    }

    //삭제 (일반 사용자) - 작성자 본인만 삭제 가능
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment findComment = commentRepository.findById(commentId)
            .orElseThrow(CommentNotFoundException::new);

        if (!findComment.getUser().getId().equals(currentUserId)) {
            throw new CommentAccessDeniedException();
        }

        deleteCommentInternal(commentId);
    }

    //삭제 (관리자) - 작성자 검사 없이 삭제, /api/admin/** 보호를 최종 권한으로 신뢰한다
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CommentNotFoundException();
        }

        deleteCommentInternal(commentId);
    }

    // 댓글 실제 삭제 공통 로직 - 대댓글이 있으면 함께 삭제한다 (기존 정책 그대로 유지)
    private void deleteCommentInternal(Long commentId) {
        commentRepository.deleteCommentsByParentCommentId(commentId);
        commentRepository.deleteById(commentId);
    }
}
