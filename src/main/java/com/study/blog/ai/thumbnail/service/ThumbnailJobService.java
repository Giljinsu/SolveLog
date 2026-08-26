package com.study.blog.ai.thumbnail.service;

import com.study.blog.client.OpenAiClient;
import com.study.blog.dto.file.FileRequestDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.exception.ThumbnailJobAccessDeniedException;
import com.study.blog.exception.ThumbnailJobInvalidStateException;
import com.study.blog.exception.ThumbnailJobNotFoundException;
import com.study.blog.service.FileService;
import com.study.blog.ai.thumbnail.domain.ThumbnailJob;
import com.study.blog.ai.thumbnail.domain.ThumbnailJobStatus;
import com.study.blog.ai.thumbnail.dto.ThumbnailJobClaimResponseDto;
import com.study.blog.ai.thumbnail.dto.ThumbnailJobCreateRequestDto;
import com.study.blog.ai.thumbnail.dto.ThumbnailJobResponseDto;
import com.study.blog.ai.thumbnail.repository.ThumbnailJobRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Thumbnail 생성 Job의 생성/조회/Worker Claim/Complete/Fail 비즈니스 로직을 담당한다.
 * 파일 저장은 기존 FileService.fileUpload()를 그대로 재사용하고, AI 전용 저장 로직을 만들지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThumbnailJobService {

    private final OpenAiClient openAiClient;
    private final ThumbnailJobRepository thumbnailJobRepository;
    private final FileService fileService;

    // 사용자 Thumbnail 생성 요청
    public ThumbnailJobResponseDto createJob(Long userId, String username,
        ThumbnailJobCreateRequestDto requestDto) {
        String userPrompt = buildUserPrompt(requestDto);
        String imagePrompt = openAiClient.generateThumbnailPrompt(userPrompt);

        String jobId = UUID.randomUUID().toString();
        ThumbnailJob job = ThumbnailJob.createQueued(jobId, userId, username, imagePrompt);

        thumbnailJobRepository.saveJob(job);
        thumbnailJobRepository.pushQueue(jobId);

        return ThumbnailJobResponseDto.from(job);
    }

    // 사용자 Job 조회 (본인 Job인지 검증)
    public ThumbnailJobResponseDto getJob(Long userId, String jobId) {
        ThumbnailJob job = findJobOrThrow(jobId);

        if (!job.getUserId().equals(userId)) {
            throw new ThumbnailJobAccessDeniedException();
        }

        return ThumbnailJobResponseDto.from(job);
    }

    // Worker Claim - Queue에서 하나 꺼내 QUEUED -> PROCESSING
    public Optional<ThumbnailJobClaimResponseDto> claimJob() {
        Optional<String> jobIdOptional = thumbnailJobRepository.popQueue();

        if (jobIdOptional.isEmpty()) {
            return Optional.empty();
        }

        String jobId = jobIdOptional.get();
        Optional<ThumbnailJob> jobOptional = thumbnailJobRepository.findJob(jobId);

        if (jobOptional.isEmpty()) {
            // TTL 만료 등으로 Job 데이터가 없는 경우 - MVP에서는 재시도/복구 없이 스킵
            log.warn("Queue에서 꺼낸 jobId에 대한 Thumbnail Job을 찾을 수 없음. jobId={}", jobId);
            return Optional.empty();
        }

        ThumbnailJob job = jobOptional.get();

        if (job.getStatus() != ThumbnailJobStatus.QUEUED) {
            // 이미 처리되었거나 Queue에 중복 push된 Job - Worker 요청 전체를 실패시키지 않고 스킵
            log.warn("QUEUED 상태가 아닌 Job이 Queue에서 발견됨. jobId={}, status={}", jobId, job.getStatus());
            return Optional.empty();
        }

        job.markProcessing();
        thumbnailJobRepository.saveJob(job);

        return Optional.of(new ThumbnailJobClaimResponseDto(job.getJobId(), job.getPrompt()));
    }

    // Worker Complete - 기존 FileService.fileUpload() 재사용, temp 저장까지만 처리
    public void completeJob(String jobId, MultipartFile image) {
        ThumbnailJob job = findJobOrThrow(jobId);
        // fileUpload 실행 전에 PROCESSING 상태인지 먼저 검증 - 중복 complete 요청에서
        // fileUpload 자체가 호출되지 않도록 markCompleted() 이전에 선검증한다.
        job.validateProcessing();

        // postId는 null -> temp 저장, 게시글 저장 시점에 기존 로직으로 S3 이동 및 postId 연결
        FileRequestDto fileRequestDto = new FileRequestDto(image, null);
        fileRequestDto.setUsername(job.getUsername());
        fileRequestDto.setIsThumbnail(true);

        FileResponseDto fileResponseDto = fileService.fileUpload(fileRequestDto);

        job.markCompleted(fileResponseDto.getFileId());
        thumbnailJobRepository.saveJob(job);
    }

    // Worker Fail
    public void failJob(String jobId, String errorMessage) {
        ThumbnailJob job = findJobOrThrow(jobId);

        job.markFailed(errorMessage);
        thumbnailJobRepository.saveJob(job);
    }

    private ThumbnailJob findJobOrThrow(String jobId) {
        return thumbnailJobRepository.findJob(jobId)
            .orElseThrow(ThumbnailJobNotFoundException::new);
    }

    private String buildUserPrompt(ThumbnailJobCreateRequestDto requestDto) {
        String problemTitle = requestDto.getProblemTitle() != null ? requestDto.getProblemTitle() : "";

        return "## 문제 제목\n\n"
            + problemTitle
            + "\n\n## 문제 설명\n\n"
            + requestDto.getProblemDescription();
    }
}
