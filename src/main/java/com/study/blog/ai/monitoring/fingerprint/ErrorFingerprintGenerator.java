package com.study.blog.ai.monitoring.fingerprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

// 동일 원인의 예외를 하나의 ErrorIssue로 묶기 위한 fingerprint 생성기(analysis.md §9, 이번 지시 §6).
// exceptionClass + 애플리케이션 코드의 첫 StackTraceElement + 정규화된 메시지 조합을 SHA-256으로 hash한다.
@Component
public class ErrorFingerprintGenerator {

    private static final String APPLICATION_PACKAGE_PREFIX = "com.study.blog";

    private static final Pattern UUID_PATTERN = Pattern.compile(
        "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\b");

    public String generate(Throwable throwable) {
        String exceptionClass = throwable.getClass().getName();
        String topApplicationFrame = findTopApplicationFrame(throwable);
        String normalizedMessage = normalizeMessage(throwable.getMessage());

        String raw = exceptionClass + ":" + topApplicationFrame + ":" + normalizedMessage;
        return sha256(raw);
    }

    private String findTopApplicationFrame(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace == null) {
            return "unknown";
        }
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().startsWith(APPLICATION_PACKAGE_PREFIX)) {
                return element.getClassName() + "#" + element.getMethodName() + ":" + element.getLineNumber();
            }
        }
        // 애플리케이션 코드 프레임이 하나도 없으면(순수 프레임워크 예외) 최상위 프레임으로 대체한다.
        if (stackTrace.length > 0) {
            StackTraceElement top = stackTrace[0];
            return top.getClassName() + "#" + top.getMethodName() + ":" + top.getLineNumber();
        }
        return "unknown";
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        String normalized = UUID_PATTERN.matcher(message).replaceAll("<UUID>");
        normalized = EMAIL_PATTERN.matcher(normalized).replaceAll("<EMAIL>");
        normalized = NUMBER_PATTERN.matcher(normalized).replaceAll("<NUM>");
        return normalized;
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다", e);
        }
    }
}
