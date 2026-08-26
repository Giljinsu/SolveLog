package com.study.blog.ai.thumbnail.domain;

import com.study.blog.exception.ThumbnailJobInvalidStateException;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Redis에 JSON 문자열로 직렬화/역직렬화되는 Thumbnail 생성 Job
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class ThumbnailJob {
    private String jobId;
    private Long userId;
    private String username;
    private ThumbnailJobStatus status;
    private String prompt;
    private Long fileId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime claimedAt;

    private ThumbnailJob(String jobId, Long userId, String username, String prompt) {
        this.jobId = jobId;
        this.userId = userId;
        this.username = username;
        this.prompt = prompt;
        this.status = ThumbnailJobStatus.QUEUED;

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ThumbnailJob createQueued(String jobId, Long userId, String username, String prompt) {
        return new ThumbnailJob(jobId, userId, username, prompt);
    }

    public void markProcessing() {
        if (this.status != ThumbnailJobStatus.QUEUED) {
            throw new ThumbnailJobInvalidStateException(
                "QUEUED 상태에서만 PROCESSING으로 전이할 수 있습니다. 현재 상태: " + this.status);
        }

        this.status = ThumbnailJobStatus.PROCESSING;
        this.claimedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // PROCESSING 상태인지 확인한다. fileUpload처럼 되돌릴 수 없는 작업 전에
    // markCompleted/markFailed와 별개로 미리 호출해서 상태를 선검증하는 용도.
    public void validateProcessing() {
        if (this.status != ThumbnailJobStatus.PROCESSING) {
            throw new ThumbnailJobInvalidStateException(
                "PROCESSING 상태의 Job만 처리할 수 있습니다. 현재 상태: " + this.status);
        }
    }

    public void markCompleted(Long fileId) {
        validateProcessing();

        this.status = ThumbnailJobStatus.COMPLETED;
        this.fileId = fileId;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        validateProcessing();

        this.status = ThumbnailJobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
}
