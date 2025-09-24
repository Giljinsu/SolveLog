package com.study.blog.entity.enums;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum FileType {
    // 이미지
    JPG("jpg", "image/jpeg"),
    JPEG("jpeg", "image/jpeg"),
    PNG("png", "image/png"),
    GIF("gif", "image/gif"),
    BMP("bmp", "image/bmp"),
    WEBP("webp", "image/webp"),

    // 문서
    PDF("pdf", "application/pdf"),
    DOC("doc", "application/msword"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PPT("ppt", "application/vnd.ms-powerpoint"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    XLS("xls", "application/vnd.ms-excel"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    TXT("txt", "text/plain"),
    HWP("hwp", "application/x-hwp"), // 한글

    // 기타
    ZIP("zip", "application/zip"),
    RAR("rar", "application/vnd.rar"),
    MP4("mp4", "video/mp4"),
    MP3("mp3", "audio/mpeg");

    private final String extension;
    private final String mimeType;

    FileType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public static Optional<FileType> getFileType(String type) {
        return Arrays.stream(values())
            .filter(fileType -> fileType.extension.equalsIgnoreCase(type)).findFirst();
        //equalsIgnoreCase = Jpg jpg JpG 혼합해도 매칭
    }

}
