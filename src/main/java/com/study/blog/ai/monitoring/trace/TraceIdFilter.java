package com.study.blog.ai.monitoring.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 요청마다 traceId를 생성해 MDC에 넣는다(analysis.md §13). 로그 수집기가 없는 지금 상태에서
// 관리자가 Monitoring 화면의 ErrorEvent.traceId로 stdout 로그를 grep해 앞뒤 흐름을
// 재구성할 수 있게 하는 것이 유일한 목적이다. Spring Security 필터 체인보다 먼저 실행되도록
// 가장 높은 우선순위로 등록한다.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_MDC_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString();
        try {
            MDC.put(TRACE_ID_MDC_KEY, traceId);
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            // Thread Pool 재사용 시 다른 요청에 traceId가 남지 않도록 반드시 제거한다.
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }
}
