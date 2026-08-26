package com.study.blog;

import com.study.blog.dto.ErrorResponse;
import com.study.blog.exception.AiGenerationException;
import com.study.blog.exception.BatchJobAlreadyRunningException;
import com.study.blog.exception.CommentAccessDeniedException;
import com.study.blog.exception.CommentNotFoundException;
import com.study.blog.exception.DuplicateLikeException;
import com.study.blog.exception.ExistUserException;
import com.study.blog.exception.InvalidTokenException;
import com.study.blog.exception.MonitoringIssueNotFoundException;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.exception.NotValidateEmailCode;
import com.study.blog.exception.NotValidateResetToken;
import com.study.blog.exception.PostAccessDeniedException;
import com.study.blog.exception.PostNotFoundException;
import com.study.blog.exception.RoleChangeNotAllowedException;
import com.study.blog.exception.ThumbnailJobAccessDeniedException;
import com.study.blog.exception.ThumbnailJobInvalidStateException;
import com.study.blog.exception.ThumbnailJobNotFoundException;
import com.study.blog.exception.UnsupportedFileTypeException;
import com.study.blog.ai.monitoring.dto.ErrorCaptureContext;
import com.study.blog.ai.monitoring.service.ErrorMonitoringFacade;
import com.study.blog.ai.monitoring.trace.TraceIdFilter;
import com.study.blog.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMonitoringFacade errorMonitoringFacade;

    @ExceptionHandler(ExistUserException.class)
    public ResponseEntity<?> handleExistUserException(ExistUserException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409 Conflict
            .body(new ErrorResponse("DUPLICATE_USERNAME", "이미 사용 중인 아이디입니다."));
    }

    //NotExistUserException

    //LockedException
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLockedException(LockedException e) {
        return ResponseEntity
            .status(HttpStatus.LOCKED) // 423
            .body(new ErrorResponse("LOCKED_USER", "잠긴 계정입니다 10분후 재시도 해주세요."));
    }

    //NotExistUserException
    @ExceptionHandler(NotExistUserException.class)
    public ResponseEntity<?> handleNotExistUserException(NotExistUserException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_EXIST_USER", "이메일이 존재하지 않습니다."));
    }

    //NotValidateEmailCode
    @ExceptionHandler(NotValidateEmailCode.class)
    public ResponseEntity<?> handleNotValidateEmailCode(NotValidateEmailCode e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_VALIDATE_EMAIL_CODE", "유효하지 않은 인증 코드입니다."));
    }

    //NotValidateResetToken
    @ExceptionHandler(NotValidateResetToken.class)
    public ResponseEntity<?> handleNotValidateResetToken(NotValidateResetToken e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_VALIDATE_RESET_TOKEN", "유효하지 않은 토큰입니다."));
    }

    //ThumbnailJobNotFoundException
    @ExceptionHandler(ThumbnailJobNotFoundException.class)
    public ResponseEntity<?> handleThumbnailJobNotFoundException(ThumbnailJobNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("THUMBNAIL_JOB_NOT_FOUND", "존재하지 않는 Thumbnail Job입니다."));
    }

    //ThumbnailJobAccessDeniedException
    @ExceptionHandler(ThumbnailJobAccessDeniedException.class)
    public ResponseEntity<?> handleThumbnailJobAccessDeniedException(ThumbnailJobAccessDeniedException e) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN) // 403
            .body(new ErrorResponse("THUMBNAIL_JOB_ACCESS_DENIED", "해당 Thumbnail Job에 접근할 권한이 없습니다."));
    }

    //ThumbnailJobInvalidStateException (잘못된 상태 전이)
    @ExceptionHandler(ThumbnailJobInvalidStateException.class)
    public ResponseEntity<?> handleThumbnailJobInvalidStateException(ThumbnailJobInvalidStateException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409
            .body(new ErrorResponse("THUMBNAIL_JOB_INVALID_STATE", "처리할 수 없는 Thumbnail Job 상태입니다."));
    }

    //RoleChangeNotAllowedException (자기 자신 ADMIN 변경 시도 / 마지막 ADMIN 보호)
    @ExceptionHandler(RoleChangeNotAllowedException.class)
    public ResponseEntity<?> handleRoleChangeNotAllowedException(RoleChangeNotAllowedException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409
            .body(new ErrorResponse("ROLE_CHANGE_NOT_ALLOWED", e.getMessage()));
    }

    //BatchJobAlreadyRunningException
    @ExceptionHandler(BatchJobAlreadyRunningException.class)
    public ResponseEntity<?> handleBatchJobAlreadyRunningException(BatchJobAlreadyRunningException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409
            .body(new ErrorResponse("BATCH_JOB_ALREADY_RUNNING", "현재 통계 Batch가 실행 중입니다."));
    }

    //PostNotFoundException
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<?> handlePostNotFoundException(PostNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("POST_NOT_FOUND", "존재하지 않는 게시글입니다."));
    }

    //PostAccessDeniedException
    @ExceptionHandler(PostAccessDeniedException.class)
    public ResponseEntity<?> handlePostAccessDeniedException(PostAccessDeniedException e) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN) // 403
            .body(new ErrorResponse("POST_ACCESS_DENIED", "본인 게시글만 삭제할 수 있습니다."));
    }

    //CommentNotFoundException
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<?> handleCommentNotFoundException(CommentNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("COMMENT_NOT_FOUND", "존재하지 않는 댓글입니다."));
    }

    //CommentAccessDeniedException
    @ExceptionHandler(CommentAccessDeniedException.class)
    public ResponseEntity<?> handleCommentAccessDeniedException(CommentAccessDeniedException e) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN) // 403
            .body(new ErrorResponse("COMMENT_ACCESS_DENIED", "본인 댓글만 삭제할 수 있습니다."));
    }

    //DuplicateLikeException - 분석(analysis.md §1/§6)에서 매핑 누락이 확인되어 이번에 추가한다.
    @ExceptionHandler(DuplicateLikeException.class)
    public ResponseEntity<?> handleDuplicateLikeException(DuplicateLikeException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409
            .body(new ErrorResponse("DUPLICATE_LIKE", "이미 좋아요를 누른 게시글입니다."));
    }

    //InvalidTokenException - 분석(analysis.md §1/§6)에서 매핑 누락이 확인되어 이번에 추가한다.
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidTokenException(InvalidTokenException e) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED) // 401
            .body(new ErrorResponse("INVALID_TOKEN", "유효하지 않은 토큰입니다. 다시 로그인해주세요."));
    }

    //MonitoringIssueNotFoundException
    @ExceptionHandler(MonitoringIssueNotFoundException.class)
    public ResponseEntity<?> handleMonitoringIssueNotFoundException(MonitoringIssueNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("MONITORING_ISSUE_NOT_FOUND", "존재하지 않는 오류 이슈입니다."));
    }

    //UnsupportedFileTypeException - bare orElseThrow() 리팩터링(FileType.getFileType())에서 확인된
    //정상적인 사용자 입력 검증 실패. 지원하지 않는 확장자 업로드는 버그가 아니다.
    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<?> handleUnsupportedFileTypeException(UnsupportedFileTypeException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400
            .body(new ErrorResponse("UNSUPPORTED_FILE_TYPE", "지원하지 않는 파일 형식입니다."));
    }

    // Spring Security 인증 실패(비밀번호 불일치 등) - 정상적인 사용자 흐름이므로 Monitoring
    // 대상에서 제외한다(ErrorClassifier). LockedException은 더 구체적인 핸들러가 이미 있어
    // 그쪽이 우선 적용된다.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED) // 401
            .body(new ErrorResponse("AUTHENTICATION_FAILED", "아이디 또는 비밀번호가 일치하지 않습니다."));
    }

    //AiGenerationException은 매핑된 핸들러가 있어도 인프라성 실패라 기록 가치가 있다
    //(analysis.md §7 ALWAYS_RECORD). ErrorAnalysisService 내부에서 발생하는 동일 예외는
    //이 핸들러를 거치지 않고(@Async 메서드는 GlobalExceptionHandler로 라우팅되지 않음)
    //자체적으로 격리되므로 순환 호출 위험이 없다.
    @ExceptionHandler(AiGenerationException.class)
    public ResponseEntity<?> handleAiGenerationExceptionWithCapture(AiGenerationException e,
        HttpServletRequest request) {
        captureIfNeeded(e, request, HttpStatus.BAD_GATEWAY.value());
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY) // 502
            .body(new ErrorResponse("AI_GENERATION_FAILED", "AI 초안 생성에 실패했습니다. 잠시 후 다시 시도해주세요."));
    }

    // 위에서 구체적으로 매핑되지 않은 모든 예외 - 지금까지 whitelabel로 새어나가던 바로 그
    // 예외들이다(analysis.md §1 핵심 발견). 반드시 기록하고, 실패해도 사용자 응답에는
    // 영향을 주지 않는다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpectedException(Exception e, HttpServletRequest request) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        log.error("예상하지 못한 서버 오류 발생 - traceId={}, exception={}, message={}, uri={}",
            traceId, e.getClass().getName(), e.getMessage(), request.getRequestURI(), e);

        captureIfNeeded(e, request, HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
            .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    // 분류/저장/분석 트리거와 그 실패 격리는 ErrorMonitoringFacade 한 곳에 모아둔다(analysis.md §12).
    private void captureIfNeeded(Throwable e, HttpServletRequest request, int httpStatus) {
        errorMonitoringFacade.report(e, buildContext(request, httpStatus));
    }

    private ErrorCaptureContext buildContext(HttpServletRequest request, int httpStatus) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY);
        Long userId = null;
        String username = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getUserId();
            username = userDetails.getUsername();
        }

        return ErrorCaptureContext.of(
            request.getRequestURI(), // 쿼리스트링은 제외 - 토큰류가 실릴 수 있다(analysis.md §15)
            request.getMethod(),
            httpStatus,
            userId,
            username,
            request.getRemoteAddr(),
            traceId
        );
    }
}
