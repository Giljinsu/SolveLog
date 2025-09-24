package com.study.blog.dto.file;

import com.study.blog.entity.enums.FileType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class FileRequestDto {
    private MultipartFile file;
    private Long postId;
    private String username;
    private Boolean isThumbnail;

    public FileRequestDto(MultipartFile file, Long postId) {
        this.file = file;
        this.postId = postId;
    }
}
