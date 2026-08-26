package com.study.blog.monitoring.fingerprint;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.blog.ai.monitoring.fingerprint.ErrorFingerprintGenerator;
import org.junit.jupiter.api.Test;

// ErrorFingerprintGenerator - 동일 원인의 예외를 하나의 ErrorIssue로 묶는 규칙 검증
// (analysis.md §9, 이번 지시 §6)
class ErrorFingerprintGeneratorTest {

    private final ErrorFingerprintGenerator generator = new ErrorFingerprintGenerator();

    @Test
    void 같은_위치_같은_타입_같은_메시지는_같은_fingerprint() {
        Throwable e1 = throwFrom("boom");
        Throwable e2 = throwFrom("boom");

        assertThat(generator.generate(e1)).isEqualTo(generator.generate(e2));
    }

    @Test
    void 다른_위치에서_발생하면_같은_예외타입_같은_메시지여도_다른_fingerprint() {
        Throwable e1 = throwFrom("boom");
        Throwable e2 = throwFromOtherLocation("boom");

        assertThat(generator.generate(e1)).isNotEqualTo(generator.generate(e2));
    }

    @Test
    void 예외타입이_다르면_같은_위치_같은_메시지여도_다른_fingerprint() {
        RuntimeException e1 = new RuntimeException("boom");
        IllegalStateException e2 = new IllegalStateException("boom");
        e2.setStackTrace(e1.getStackTrace());

        assertThat(generator.generate(e1)).isNotEqualTo(generator.generate(e2));
    }

    @Test
    void 메시지의_숫자ID는_정규화되어_같은_fingerprint가_된다() {
        Throwable e1 = throwFrom("user 123 not found");
        Throwable e2 = throwFrom("user 999 not found");

        assertThat(generator.generate(e1)).isEqualTo(generator.generate(e2));
    }

    @Test
    void 메시지의_이메일은_정규화되어_같은_fingerprint가_된다() {
        Throwable e1 = throwFrom("failed for a@example.com");
        Throwable e2 = throwFrom("failed for b@other.co.kr");

        assertThat(generator.generate(e1)).isEqualTo(generator.generate(e2));
    }

    @Test
    void 메시지의_UUID는_정규화되어_같은_fingerprint가_된다() {
        Throwable e1 = throwFrom("job 550e8400-e29b-41d4-a716-446655440000 failed");
        Throwable e2 = throwFrom("job 6ba7b810-9dad-11d1-80b4-00c04fd430c8 failed");

        assertThat(generator.generate(e1)).isEqualTo(generator.generate(e2));
    }

    @Test
    void 완전히_다른_메시지는_다른_fingerprint가_된다() {
        Throwable e1 = throwFrom("out of memory");
        Throwable e2 = throwFrom("connection refused");

        assertThat(generator.generate(e1)).isNotEqualTo(generator.generate(e2));
    }

    // com.study.blog 패키지가 아니므로 애플리케이션 프레임이 없을 때도 예외 없이 동작해야 한다.
    @Test
    void 애플리케이션_프레임이_없어도_예외없이_fingerprint를_생성한다() {
        RuntimeException e = new RuntimeException("no app frame");
        e.setStackTrace(new StackTraceElement[]{
            new StackTraceElement("java.util.Optional", "orElseThrow", "Optional.java", 382)
        });

        assertThat(generator.generate(e)).isNotBlank();
    }

    private Throwable throwFrom(String message) {
        return new RuntimeException(message);
    }

    private Throwable throwFromOtherLocation(String message) {
        return new RuntimeException(message);
    }
}
