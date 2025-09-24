package com.study.blog.service;

import com.study.blog.dto.comment.CommentResponseDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.dto.post.PostRequestDto;
import com.study.blog.dto.post.PostResponseDto;
import com.study.blog.dto.post.PostSliceResponseDto;
import com.study.blog.dto.post.SearchCondition;
import com.study.blog.dto.tag.TagResponseDto;
import com.study.blog.entity.Category;
import com.study.blog.entity.Comment;
import com.study.blog.entity.File;
import com.study.blog.entity.Post;
import com.study.blog.entity.PostTag;
import com.study.blog.entity.Tag;
import com.study.blog.entity.Users;
import com.study.blog.repository.CategoryRepository;
import com.study.blog.repository.CommentRepository;
import com.study.blog.repository.FileRepository;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.PostTagRepository;
import com.study.blog.repository.TagRepository;
import com.study.blog.repository.UsersRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final UsersRepository usersRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final FileRepository fileRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    private final EntityManager em;
    private final FileService fileService;
    private final CommentService commentService;

    //게시글 검색 조건(제목 혹은 내용?) 최신순 좋아요순 조회수순 등
    public PostSliceResponseDto getList(SearchCondition searchCondition, Pageable pageable) {
        // 태그 검색시
        if (searchCondition.getTagNameList() != null) {
            List<Tag> tagList = tagRepository.findByNameInIgnoreCase(
                searchCondition.getTagNameList());

            List<Long> tagIdList = tagList.stream().map(Tag::getId).toList();

            searchCondition.setTagIdList(tagIdList);
        }

        Slice<PostResponseDto> listWithSearchCondition = postRepository.findListWithSearchCondition(
            searchCondition, pageable);

        List<PostResponseDto> content = listWithSearchCondition.getContent();

//        content.forEach(post -> post.addTagList(postTagRepository.findByPostId(post.getId()).orElseThrow()));

        // tag
        Map<Long, List<TagResponseDto>> tagsPerPostIdMap = getTagsPerPostIdMap(content);

        // 유저 이미지
        Set<String> usernameSet = content.stream().map(PostResponseDto::getUsername)
            .collect(Collectors.toSet());

        Map<String, FileResponseDto> userImgMap = fileRepository.findUserImgListByUsernames(
                usernameSet)
            .stream().collect(Collectors.toMap(
                File::getUsername,
                file -> new FileResponseDto(file.getId(), file.getOriginalFileName(),
                    file.getPath())
            ));

        content.forEach(post -> {
            post.addTagList(tagsPerPostIdMap.get(post.getId()));
            post.setUserImg(userImgMap.get(post.getUsername()));
        });

        return new PostSliceResponseDto(content, listWithSearchCondition.hasNext());
    }


    //게시글 상세 검색 -> 게시글 한개
    public PostResponseDto getDetailPost(Long postId) {
        PostResponseDto postDto = postRepository.findDetailPostById(postId);

        postDto.addTagList(postTagRepository.findByPostId(postDto.getId()).orElseThrow());

        // comment 리스트
        // dto 변환
        List<Comment> comments = commentRepository.findCommentByPostId(postId);

        Map<String, FileResponseDto> userImgMap = commentService.getUserImgMap(comments);


        List<CommentResponseDto> commentResponseDto = comments
            .stream().map(comment ->
                new CommentResponseDto(
                    comment.getId(),
                    comment.getUser().getNickname(),
                    comment.getUser().getUsername(),
                    comment.getComment(),
                    comment.getCreatedDate(),
                    comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                    comment.getChildComments() != null ?
                        comment.getChildComments().stream().map(childComment -> new CommentResponseDto(
                            childComment.getId(),
                            childComment.getUser().getNickname(),
                            childComment.getUser().getUsername(),
                            childComment.getComment(),
                            childComment.getCreatedDate(),
                            childComment.getParentComment() != null ? childComment.getParentComment().getId() : null,
                            userImgMap.get(childComment.getUser().getUsername())
                        )).toList() : null,
                    userImgMap.get(comment.getUser().getUsername())
                )
            ).toList();

        // 파일 리스트
        Optional<List<File>> fileByPostId = fileRepository.findFileByPostId(postId);

        List<FileResponseDto> fileResponseDto = new ArrayList<>();
        if (fileByPostId.isPresent()) {
            fileResponseDto = fileByPostId.get()
                .stream().map(file -> new FileResponseDto(
                    file.getId(),
                    file.getPostId(),
                    file.getType(),
                    file.getOriginalFileName(),
                    file.getSize(),
                    file.getIsThumbnail()
                )).toList();
        }

        if (!commentResponseDto.isEmpty()) {
            postDto.addComments(commentResponseDto);
        }

        if (!fileResponseDto.isEmpty()) {
            postDto.addFiles(fileResponseDto);
        }

        return postDto;
    }

    public int getViews(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();

        return post.getViewCount();
    }

    //임시 게시글 조회
    public PostSliceResponseDto getTmpPostList(String username, Pageable pageable) {
        Slice<PostResponseDto> tmpPostList = postRepository.findByUsernameAndIsTemp(username,
            pageable);

        return new PostSliceResponseDto(tmpPostList.getContent(), tmpPostList.hasNext());
//        return null;
    }

    //게시글 좋아요한 게시글 검색
    public PostSliceResponseDto getLikePostList(SearchCondition searchCondition, Pageable pageable) {
        // 태그 검색시
        if (searchCondition.getTagNameList() != null) {
            List<Tag> tagList = tagRepository.findByNameInIgnoreCase(
                searchCondition.getTagNameList());

            List<Long> tagIdList = tagList.stream().map(Tag::getId).toList();

            searchCondition.setTagIdList(tagIdList);
        }

        Slice<PostResponseDto> listWithSearchCondition = postRepository.findLikesList(
            searchCondition, pageable);

        List<PostResponseDto> content = listWithSearchCondition.getContent();

//        content.forEach(post -> post.addTagList(postTagRepository.findByPostId(post.getId()).orElseThrow()));

        // tag
        Map<Long, List<TagResponseDto>> tagsPerPostIdMap = getTagsPerPostIdMap(content);

        // 유저 이미지
        Set<String> usernameSet = content.stream().map(PostResponseDto::getUsername)
            .collect(Collectors.toSet());

        Map<String, FileResponseDto> userImgMap = fileRepository.findUserImgListByUsernames(
                usernameSet)
            .stream().collect(Collectors.toMap(
                File::getUsername,
                file -> new FileResponseDto(file.getId(), file.getOriginalFileName(),
                    file.getPath())
            ));

        content.forEach(post -> {
            post.addTagList(tagsPerPostIdMap.get(post.getId()));
            post.setUserImg(userImgMap.get(post.getUsername()));
        });

        return new PostSliceResponseDto(content, listWithSearchCondition.hasNext());
    }

    //tagId로 해당 유저의 게시글 조회
    public PostSliceResponseDto getPostByTagIdAndUsername(SearchCondition searchCondition, Pageable pageable) {
        Slice<PostResponseDto> postList = postRepository.getPostByTagIdAndUsername(searchCondition, pageable);

        List<PostResponseDto> content = postList.getContent();

        Map<Long, List<TagResponseDto>> tagsPerPostIdMap = getTagsPerPostIdMap(content);

        content.forEach(post -> post.addTagList(tagsPerPostIdMap.get(post.getId())));


        return new PostSliceResponseDto(content, postList.hasNext());
    }

    private Map<Long, List<TagResponseDto>> getTagsPerPostIdMap(List<PostResponseDto> content) {
        Set<Long> postIdSet = content.stream().map(PostResponseDto::getId)
            .collect(Collectors.toSet());

        List<TagResponseDto> tagsPerPostId = postTagRepository.findByPostIdSet(postIdSet);

        return tagsPerPostId.stream()
            .collect(Collectors.groupingBy(
                TagResponseDto::getPostId,
                Collectors.mapping(
                    tag -> new TagResponseDto(tag.getTagId(), tag.getTagName()),
                    Collectors.toList()
                )
            ));
    }

    @Transactional
    public int addView(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();

        int newViewCount = post.getViewCount();
        post.setViews(++newViewCount);

        return newViewCount;
    }


    //게시글 생성
    // stream 연습
    @Transactional
    public Long createPost(PostRequestDto requestDto) {
        Users findUser = getFindUser(requestDto);
        Category findCategory = getFindCategory(requestDto);

        // 세로운 태그들
        Set<String> tagNameSet = Arrays.stream(
                Optional.ofNullable(requestDto.getTags()).orElse("")
                    .split(",")
            ).map(String::trim)
            .filter(tagName -> !tagName.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        Post newPost = Post.createPost(requestDto.getTitle(), requestDto.getContent(), findUser,
            findCategory, requestDto.getTags(), requestDto.getSummary(),
            requestDto.getIsTemp() != null ? requestDto.getIsTemp() : true);

        postRepository.save(newPost);

        upsertTag(tagNameSet, newPost);

//        em.flush();
//        em.clear();

        // 커밋 후 실행 -> dB 오류발생시 파일 이동을 막기위함
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
//                    afterCommit은 트랜잭션이 완전히 커밋되고 영속성 컨텍스트가 닫힌 다음 호출된다.
                    cleanUnusedFile(requestDto.getUsername(), requestDto.getContent());
                    fileService.updateFilePostId(newPost.getId(), requestDto.getUsername());

                    //임시파일 이동
                    fileService.moveFileToPostId(newPost.getId());
                }
            }

        );

        return newPost.getId();
    }

    private void upsertTag(Set<String> tagNameSet, Post newPost) {
        if (!tagNameSet.isEmpty()) {
            List<Tag> findTags = tagRepository.findByNameInIgnoreCase(tagNameSet);
            // 새로운 태그들 중 있는애들만
            Map<String, Tag> findTagsMap = findTags.stream()
                .collect(Collectors.toMap(tag -> tag.getName().toLowerCase(), Function.identity()));

            // 불일치 찾은 후 저장
            // 새로운 태그중 없는 애들
            List<Tag> newTags = tagNameSet
                .stream()
                .filter(newTag -> !findTagsMap.containsKey(newTag))
                .map(Tag::createTag).toList();

            if (!newTags.isEmpty()) {
                try {
                    List<Tag> saveTags = tagRepository.saveAll(newTags);
                    tagRepository.flush();
                    saveTags.forEach(st -> findTagsMap.put(st.getName().toLowerCase(), st));
                } catch (DataIntegrityViolationException e) { // DataIntegrityViolationException : SQL 문이 잘못되었거나 Data 가 잘못되었을경우
                    //동시성 오류 동시에 넣었을경우 대비
                    List<Tag> reFetch = tagRepository.findByNameInIgnoreCase(tagNameSet);
                    findTagsMap.clear();
                    reFetch.forEach(t -> findTagsMap.put(t.getName().toLowerCase(), t));

                    List<String> stillMissing = tagNameSet.stream().filter(t -> !findTagsMap.containsKey(t))
                        .toList();

                    for (String s : stillMissing) {
                        try {
                            Tag save = tagRepository.save(Tag.createTag(s));
                            tagRepository.flush();
                            findTagsMap.put(s.toLowerCase(), save);
                        } catch (DataIntegrityViolationException e2) {
                            // 또발생시
                            Tag existing = tagRepository.findByNameIgnoreCase(s);
                            if (existing != null) {
                                findTagsMap.put(existing.getName().toLowerCase(), existing);
                            } else {
                                log.warn("Tag not resolve after retry {}", s);
                            }

                        }
                    }


                }
            }

            List<PostTag> newPostTags = tagNameSet.stream()
                .map(t -> PostTag.createPostTag(newPost, findTagsMap.get(t.toLowerCase()))).toList();

            postTagRepository.saveAll(newPostTags);

        }
    }

    //게시글 생성
    @Transactional
    public Long createPost_Old_TagsStringVer(PostRequestDto requestDto) {
        Users findUser = getFindUser(requestDto);
        Category findCategory = getFindCategory(requestDto);

        Post newPost = Post.createPost(requestDto.getTitle(), requestDto.getContent(), findUser,
            findCategory, requestDto.getTags(), requestDto.getSummary(),
            requestDto.getIsTemp() != null ? requestDto.getIsTemp() : true);

        postRepository.save(newPost);

//        em.flush();
//        em.clear();

        cleanUnusedFile(requestDto.getUsername(), requestDto.getContent());
        fileService.updateFilePostId(newPost.getId(), requestDto.getUsername());

        //임시파일 이동
        fileService.moveFileToPostId(newPost.getId());

        return newPost.getId();
    }

//  afterCommit은 트랜잭션이 완전히 커밋되고 영속성 컨텍스트가 닫힌 다음 호출. 따라서 벌크성 업데이트 처리
//    private void updateFilePostId(Long postId, String username) {
//
//        fileRepository.bulkUpdatePostIdWherePostIdIsNull(postId, username);
//    }

    private Set<Long> extractFileIdsFromMarkdown(String content) {
        Set<Long> fileIds = new HashSet<>();
        // hashset 사용 이유 : 중복 X 와 Contain 여부 빠르게 가능
        Pattern compile = Pattern.compile("/api/inlineFile/(\\d+)");
        Matcher matcher = compile.matcher(content);

        while (matcher.find()) {
            Long fileId = Long.valueOf(matcher.group(1));
            fileIds.add(fileId);
        }

        return fileIds;
    }

    // 임시파일 삭제
    private void cleanUnusedFile(String username, String content) {
        Set<Long> fileIds = extractFileIdsFromMarkdown(content);
        List<File> files = fileRepository.findByUsernameAndPostIdIsNull(username).orElseThrow();

        for (File file : files) {
            if (!fileIds.contains(file.getId())) {
                java.io.File physicalFile = new java.io.File(file.getPath());
                if (physicalFile.exists() && !file.getIsThumbnail() && !file.getIsUserImg()) { //유저이미지 썸네일을 제외한 삭제 썸네일은 프론트에서 삭제
                    fileService.deleteFile(file.getId());
                }
            }
        }


    }

    // 임시파일이 아닌 postId가 있는것 중 없는것 삭제
    private void cleanUnusedExistFile(Long postId, String content) {
        List<File> existingFiles = fileRepository.findFileByPostId(postId).orElseThrow();
        Set<Long> fileIds = extractFileIdsFromMarkdown(content);

        // 기존 있는 파일중 없는것 삭제
        for (File existingFile : existingFiles) {
            if (!fileIds.contains(existingFile.getId())) {
                java.io.File physicalFile = new java.io.File(existingFile.getPath());
                if (physicalFile.exists() && !existingFile.getIsThumbnail()) { // 썸네일을 제외한 삭제 썸네일은 프론트에서 삭제
                    fileService.deleteFile(existingFile.getId());
                }
            }
        }
    }

    //게시글 수정
    @Transactional
    public Long updatePost(PostRequestDto requestDto) {
        Post findPost = postRepository.findById(requestDto.getPostId()).orElseThrow();
        Category findCategory = getFindCategory(requestDto);

        // 세로운 태그들
        Set<String> tagNameSet = Arrays.stream(
                Optional.ofNullable(requestDto.getTags()).orElse("")
                    .split(",")
            ).map(String::trim)
            .filter(tagName -> !tagName.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        findPost.updatePost(requestDto.getTitle(), requestDto.getContent(), findCategory,
            requestDto.getTags(), requestDto.getSummary(), requestDto.getIsTemp());

        postTagRepository.deleteByPostId(findPost.getId());

        upsertTag(tagNameSet, findPost);



        // 임시파일 삭제
        cleanUnusedFile(requestDto.getUsername(), requestDto.getContent());
        // 업데이트 후 없는것 삭제
        cleanUnusedExistFile(requestDto.getPostId(), requestDto.getContent());

        //임시파일 이동
        fileService.moveFileToPostId(requestDto.getPostId());

        fileService.updateFilePostId(requestDto.getPostId(), requestDto.getUsername());

        return findPost.getId();
    }

    private Users getFindUser(PostRequestDto requestDto) {
        return usersRepository.findUsersByUsername(requestDto.getUsername()).orElseThrow();
    }

    private Category getFindCategory(PostRequestDto requestDto) {
        return categoryRepository.findByType(requestDto.getCategoryType());
    }

    //게시글 삭제
    /*
        벌크성 삭제를 사용해야함
        그냥 스프링 데이터 jpa 가 만들어주는 건 조회 후 삭제가 일어나기 때문에 n+1이 발생
     */
    @Transactional
    public void deletePost(Long id) {
        // 댓글 삭제
        List<Comment> comments = commentRepository.findCommentByPostId(id);

        if (!comments.isEmpty()) {
            commentRepository.deleteByPostId(id);
        }

        postTagRepository.deleteByPostId(id);

        fileService.deleteFilesByPostId(id);


//        em.flush();
//        em.clear();

        postRepository.deleteById(id);
    }



}
