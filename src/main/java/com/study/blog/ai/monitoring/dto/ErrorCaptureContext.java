package com.study.blog.ai.monitoring.dto;

// Error 발생 시점의 요청 컨텍스트. HTTP 요청이 없는 호출(Batch 등)은 requestUri에
// "batch://{jobName}" 같은 값을 넣고 나머지는 null로 둔다. 민감정보(요청 바디/헤더/쿠키)는
// 절대 포함하지 않는다(analysis.md §15).
public record ErrorCaptureContext(
    String requestUri,
    String httpMethod,
    Integer httpStatus,
    Long userId,
    String username,
    String ip,
    String traceId
) {
    public static ErrorCaptureContext of(String requestUri, String httpMethod, Integer httpStatus,
        Long userId, String username, String ip, String traceId) {
        return new ErrorCaptureContext(requestUri, httpMethod, httpStatus, userId, username, ip, traceId);
    }

    public static ErrorCaptureContext forBatch(String jobName, String traceId) {
        return new ErrorCaptureContext("batch://" + jobName, null, null, null, null, null, traceId);
    }
}
