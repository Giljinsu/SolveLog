package com.study.blog.monitoring.classifier;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.blog.ai.monitoring.classifier.ErrorAction;
import com.study.blog.ai.monitoring.classifier.ErrorClassifier;
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
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;

// ErrorClassifier - "이 예외를 Error Monitoring에 저장할지"를 결정하는 유일한 지점 검증
// (analysis.md §7, 이번 지시 §2)
class ErrorClassifierTest {

    private final ErrorClassifier classifier = new ErrorClassifier();

    @Test
    void 비즈니스적으로_예상된_예외는_모두_IGNORE() {
        assertThat(classifier.classify(new ExistUserException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new LockedException("locked"))).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new NotExistUserException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new NotValidateEmailCode())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new NotValidateResetToken())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new ThumbnailJobNotFoundException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new ThumbnailJobAccessDeniedException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new ThumbnailJobInvalidStateException("x"))).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new RoleChangeNotAllowedException("x"))).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new BatchJobAlreadyRunningException("x"))).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new PostNotFoundException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new PostAccessDeniedException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new CommentNotFoundException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new CommentAccessDeniedException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new DuplicateLikeException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new InvalidTokenException("x"))).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new MonitoringIssueNotFoundException())).isEqualTo(ErrorAction.IGNORE);
        assertThat(classifier.classify(new UnsupportedFileTypeException())).isEqualTo(ErrorAction.IGNORE);
    }

    @Test
    void 인증_실패는_AuthenticationException_계열이면_전부_IGNORE() {
        assertThat(classifier.classify(new BadCredentialsException("wrong password")))
            .isEqualTo(ErrorAction.IGNORE);
    }

    @Test
    void AiGenerationException은_매핑된_핸들러가_있어도_RECORD_AND_AI_ANALYZE() {
        assertThat(classifier.classify(new AiGenerationException("gpt fail")))
            .isEqualTo(ErrorAction.RECORD_AND_AI_ANALYZE);
    }

    @Test
    void 매핑되지_않은_예외는_기본값으로_RECORD_AND_AI_ANALYZE() {
        assertThat(classifier.classify(new NullPointerException("npe")))
            .isEqualTo(ErrorAction.RECORD_AND_AI_ANALYZE);
        assertThat(classifier.classify(new RuntimeException("unexpected")))
            .isEqualTo(ErrorAction.RECORD_AND_AI_ANALYZE);
        assertThat(classifier.classify(new java.util.NoSuchElementException()))
            .isEqualTo(ErrorAction.RECORD_AND_AI_ANALYZE);
    }
}
