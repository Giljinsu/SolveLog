package com.study.blog.entity;

import com.study.blog.entity.enums.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class File {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    private FileType type;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String username;

//    @Column(nullable = false)
    private Long postId;

    private Long size;

    private LocalDateTime uploadDate;

    private Boolean isThumbnail;

    private Boolean isUserImg;

    private File(String path, String username, Long postId, FileType type, String originalFileName,
        Long size, Boolean isThumbnail, Boolean isUserImg) {
        this.path = path;
        this.postId = postId;
        this.username = username;
        this.type = type;
        this.originalFileName = originalFileName;
        this.size = size;
        this.isThumbnail = isThumbnail;
        this.isUserImg = isUserImg;
    }

    public static File createFile(String path, String username, Long postId, FileType type,
        String originalFileName, Long size, Boolean isThumbnail, Boolean isUserImg) {
        return new File(path, username, postId, type, originalFileName, size, isThumbnail, isUserImg);
    }

    public void insertPostId(Long postId) {
        this.postId = postId;
    }

    public void updatePath(String path) {
        this.path = path;
    }

    @PrePersist
    public void setUploadDate() {
        this.uploadDate = LocalDateTime.now();
    }
}


