package com.study.blog.ai.thumbnail.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Mac Worker 전용 인증 필터. /api/internal/thumbnail-jobs/** 요청의 Authorization 헤더를
 * THUMBNAIL_WORKER_TOKEN과 단순 비교한다. UsersService/CustomUserDetails/JwtUtil/DB는 사용하지 않는다.
 * /api/internal/thumbnail-jobs 이외 경로는 그대로 통과시켜 기존 사용자 인증 흐름에 영향을 주지 않는다.
 *
 * 의도적으로 @Component로 등록하지 않는다 - Spring이 관리하는 Filter 타입 Bean이 되면
 * Spring Boot가 서블릿 컨테이너에 이 필터를 /* 로 자동 등록해버리므로(SecurityFilterChain 내부
 * 등록과 별개로 이중 등록됨), SecurityConfig의 internalWorkerFilterChain 빈 안에서만
 * new로 생성해서 사용한다.
 */
public class WorkerTokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String INTERNAL_PATH_PREFIX = "/api/internal/thumbnail-jobs";

    private final String workerToken;

    public WorkerTokenAuthenticationFilter(String workerToken) {
        this.workerToken = workerToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.equals("Bearer " + workerToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid worker token\"}");
            return;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            "thumbnail-worker",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_WORKER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
