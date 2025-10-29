package com.study.blog.service;

import com.study.blog.exception.NotExistUserException;
import com.study.blog.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //OncePerRequestFilter 요청 하나(HTTP request)당 딱 한 번만 실행되는 필터다.
    private final JwtUtil jwtUtil;
    private final UsersService usersService;

    /*
        클라이언트가 요청했을 때
        Authorization: Bearer <AccessToken>
     */


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

//        String authHeader = request.getHeader("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        String token = authHeader.substring(7); // "Bearer " 이후

        if ("/api/logout".equals(request.getRequestURI()) || "/api/refresh".equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtUtil.extractTokenFromCookie(request, "accessToken");

//        if (token == null) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        if (!jwtUtil.isTokenValid(token)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
//            response.setContentType("application/json"); // json 응답
//            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
//
////            filterChain.doFilter(request, response); // 다음 필터
//            return;
//        }

        if (token == null || !jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtUtil.getUsername(token);

        try {
            UserDetails userDetails = usersService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails // userDetails를 넣어줘야 @AuthenticationPrincipal 을 사용 가능
                ,null
                ,userDetails.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            /*
                WebAuthenticationDetailsSource() 에는
                remoteAddress 클라언트 IP 주소
                sessionId 요청에 포람된 세션 ID
                JWT는 세션을 안쓰니 sessionId는 별 의미 없음

               하지만 Spring Security 내부 로직 중엔 details 필드가 null이면 NullPointerException 터지는 경우도 있음
               (예: 커스텀 AuthenticationSuccessHandler 같은 데서)

                방어 코드처럼 안전하게 넣어주는게 관행
             */
            SecurityContextHolder.getContext().setAuthentication(authToken);


            filterChain.doFilter(request,response);

        } catch (NotExistUserException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json"); // json 응답
            response.getWriter().write("{\"error\": \"not found username\"}");

        }
    }

}
