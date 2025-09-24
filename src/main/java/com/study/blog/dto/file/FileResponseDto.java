package com.study.blog.dto.file;

import com.study.blog.entity.enums.FileType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FileResponseDto {
    private Long fileId;
    private Long postId;
    private FileType type;
    private String originalFileName;
    private Long size;
    private String path;
    private Boolean isThumbnail;

    public FileResponseDto(Long fileId, Long postId, FileType type, String originalFileName,
        Long size, Boolean isThumbnail) {
        this.fileId = fileId;
        this.postId = postId;
        this.type = type;
        this.originalFileName = originalFileName;
        this.size = size;
        this.isThumbnail = isThumbnail;
    }

    public FileResponseDto(Long fileId, Long postId, String originalFileName,
        String path) {
        this.fileId = fileId;
        this.postId = postId;
        this.originalFileName = originalFileName;
        this.path = path;
    }

    //유저이미지
    public FileResponseDto(Long fileId, String originalFileName,
        String path) {
        this.fileId = fileId;
        this.originalFileName = originalFileName;
        this.path = path;
    }
}
