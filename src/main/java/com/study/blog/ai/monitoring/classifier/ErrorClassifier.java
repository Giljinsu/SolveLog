package com.study.blog.ai.monitoring.classifier;

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
import java.util.Set;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

/**
 * "이 예외를 Error Monitoring에 저장할지"를 결정하는 유일한 지점(analysis.md §7).
 * HTTP Status가 아니라 예외 타입 기준으로 판단한다 - 같은 409/403이라도 비즈니스적으로
 * 예상된 흐름(RoleChangeNotAllowedException 등)과 진짜 버그를 구분할 수 없기 때문이다.
 *
 * GlobalExceptionHandler에서 이미 구체적으로 매핑된 예외는 Spring이 그 구체적인
 * @ExceptionHandler로 먼저 라우팅하므로 catch-all(Exception.class)에는 애초에 도달하지
 * 않는다. 즉 catch-all에 들어온 시점에 이미 "매핑되지 않은 예외"이므로 기본값은
 * RECORD_AND_AI_ANALYZE다. 아래 IGNORE_EXCEPTIONS는 향후 이 Classifier가 catch-all
 * 이외의 경로(배치 등)에서도 재사용될 것을 대비한 방어적 화이트리스트다.
 */
@Component
public class ErrorClassifier {

    // 비즈니스적으로 예상 가능한 흐름 - 절대 저장하지 않는다.
    private static final Set<Class<? extends Throwable>> IGNORE_EXCEPTIONS = Set.of(
        ExistUserException.class,
        LockedException.class,
        NotExistUserException.class,
        NotValidateEmailCode.class,
        NotValidateResetToken.class,
        ThumbnailJobNotFoundException.class,
        ThumbnailJobAccessDeniedException.class,
        ThumbnailJobInvalidStateException.class,
        RoleChangeNotAllowedException.class,
        BatchJobAlreadyRunningException.class,
        PostNotFoundException.class,
        PostAccessDeniedException.class,
        CommentNotFoundException.class,
        CommentAccessDeniedException.class,
        DuplicateLikeException.class,
        InvalidTokenException.class,
        MonitoringIssueNotFoundException.class,
        UnsupportedFileTypeException.class,
        AsyncRequestTimeoutException.class
    );

    // 매핑된 핸들러가 있어도 인프라성 실패라 기록 가치가 있는 예외 (analysis.md §7).
    private static final Set<Class<? extends Throwable>> ALWAYS_RECORD_EXCEPTIONS = Set.of(
        AiGenerationException.class
    );

    public ErrorAction classify(Throwable throwable) {
        Class<? extends Throwable> type = throwable.getClass();

        if (ALWAYS_RECORD_EXCEPTIONS.contains(type)) {
            return ErrorAction.RECORD_AND_AI_ANALYZE;
        }

        if (IGNORE_EXCEPTIONS.contains(type)) {
            return ErrorAction.IGNORE;
        }

        // Spring Security 인증 실패(비밀번호 오류 등)는 정상적으로 발생하는 사용자 흐름이다.
        if (throwable instanceof AuthenticationException) {
            return ErrorAction.IGNORE;
        }

        return ErrorAction.RECORD_AND_AI_ANALYZE;
    }
}
