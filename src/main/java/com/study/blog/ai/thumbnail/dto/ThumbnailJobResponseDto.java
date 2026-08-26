package com.study.blog.ai.thumbnail.dto;

import com.study.blog.ai.thumbnail.domain.ThumbnailJob;
import com.study.blog.ai.thumbnail.domain.ThumbnailJobStatus;
import lombok.Getter;

@Getter
public class ThumbnailJobResponseDto {
    private final String jobId;
    private final ThumbnailJobStatus status;
    private final Long fileId;
    private final String imageUrl;
    private final String errorMessage;

    private ThumbnailJobResponseDto(String jobId, ThumbnailJobStatus status, Long fileId,
        String imageUrl, String errorMessage) {
        this.jobId = jobId;
        this.status = status;
        this.fileId = fileId;
        this.imageUrl = imageUrl;
        this.errorMessage = errorMessage;
    }

    public static ThumbnailJobResponseDto from(ThumbnailJob job) {
        String imageUrl = job.getFileId() != null ? "/api/inlineFile/" + job.getFileId() : null;

        return new ThumbnailJobResponseDto(
            job.getJobId(),
            job.getStatus(),
            job.getFileId(),
            imageUrl,
            job.getErrorMessage()
        );
    }
}
