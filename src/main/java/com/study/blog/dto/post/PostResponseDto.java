package com.study.blog.dto.post;

import com.study.blog.dto.comment.CommentResponseDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.dto.tag.TagResponseDto;
import com.study.blog.entity.PostTag;
import com.study.blog.entity.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String nickName;
    private String authorBio;
    private String username;
    private String categoryType;
    private String tags;
    private String summary;
    private Boolean isTemp;
    private List<CommentResponseDto> comments;
    private List<FileResponseDto> files;
    private FileResponseDto thumbnailFile;
    private List<TagResponseDto> tagList;
    private FileResponseDto userImg;
    //tagList

    // 리스트 조회용
    public PostResponseDto(Long id, String title, String content, LocalDateTime createdDate,
        Integer viewCount, String categoryType, Integer likeCount, Integer commentCount,
        String nickName, String username, String tags, String summary, FileResponseDto thumbnailFile) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
        this.categoryType = categoryType;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.nickName = nickName;
        this.username = username;
        this.tags = tags;
        this.summary = summary;
        this.thumbnailFile = thumbnailFile;
    }

    // 임시 리스트 조회용
    public PostResponseDto(Long id, String title, String content, LocalDateTime createdDate,
        String tags, String summary, FileResponseDto thumbnailFile) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.tags = tags;
        this.summary = summary;
        this.thumbnailFile = thumbnailFile;
    }

    // 상세
    public PostResponseDto(Long id, String title, String content, LocalDateTime createdDate,
        Integer viewCount, String categoryType, Integer likeCount, Integer commentCount,
        String nickName, String username, String authorBio, String tags, String summary, Boolean isTemp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.viewCount = viewCount;
        this.categoryType = categoryType;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.nickName = nickName;
        this.username = username;
        this.authorBio = authorBio;
        this.tags = tags;
        this.summary = summary;
        this.isTemp = isTemp;
    }


    public void addComments(List<CommentResponseDto> comments) {
        this.comments = comments;
    }

    public void addFiles(List<FileResponseDto> files) {
        this.files = files;
    }

//    public void addTags(String tagString) {this.tags = tags; }

    public void addTagList(List<TagResponseDto> tagList) { this.tagList = tagList; }

}
