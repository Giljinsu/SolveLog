package com.study.blog.config;

import com.study.blog.service.CustomAuthenticationProvider;
import com.study.blog.service.JwtAuthenticationFilter;
import com.study.blog.ai.thumbnail.security.WorkerTokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    세션 기반용
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http,
//        AuthenticationProvider authenticationProvider,
//        LoginSuccessHandler loginSuccessHandler,
//        LoginFailedHandler loginFailedHandler,
//        CustomLogoutSuccessHandler logoutSuccessHandler) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests((auth) -> auth
//                    .requestMatchers("/login", "/signup", "/api/createUser").permitAll()
//                    .anyRequest().authenticated()
////                .anyRequest().permitAll()
//            )
//            .formLogin(form -> form
//                .loginProcessingUrl("/login") // 로그인 POST 요청
//                .successHandler(loginSuccessHandler)
//                .failureHandler(loginFailedHandler)
//                .permitAll()
//            )
//            .logout(logout -> logout
//                .logoutUrl("/logout")                    // POST 요청
//                .logoutSuccessHandler(logoutSuccessHandler)       // 리다이렉트
//                .invalidateHttpSession(true)             // 세션 무효화
//                .deleteCookies("JSESSIONID") // 쿠키 삭제
//            )
//            .authenticationProvider(authenticationProvider); // 커스텀 AuthenticationProvider 시큐리티 등록
//
//        return http.build();
//    }

    // Mac Worker 전용 - /api/internal/thumbnail-jobs/** 는 사용자 JWT 인증과 완전히 분리된
    // Worker Token 인증만 사용한다. 기존 filterChain(@Order(2))에는 영향을 주지 않는다.
    @Bean
    @Order(1)
    public SecurityFilterChain internalWorkerFilterChain(HttpSecurity http,
        @Value("${thumbnail.worker.token}") String workerToken) throws Exception {
        // Spring이 관리하는 Bean으로 등록하지 않고 여기서 직접 생성한다 -
        // Filter 타입 Bean이 되면 Spring Boot가 서블릿 컨테이너에 이중 등록하기 때문.
        WorkerTokenAuthenticationFilter workerTokenAuthenticationFilter =
            new WorkerTokenAuthenticationFilter(workerToken);

        http
            .securityMatcher("/api/internal/thumbnail-jobs/**")
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(workerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http,
        JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Spring Security에서 CORS 허용
            .csrf(csrf -> csrf.disable())
            .formLogin(AbstractHttpConfigurer::disable) // 폼로그인 비활성화 이거 안 끄면 자동 필터 작동 가능
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((auth) -> auth
                    .requestMatchers(
                        "/error",
                        "/api/login",
                        "/api/signup",
                        "/api/createUser",
                        "/api/hello",
                        "/api/getPostList",
                        "/api/getPostDetail/**", // /** 하위경로
                        "/api/getComments/**",
                        "/api/posts/**",
                        "/api/getCategoryListByParentType/**",
                        "/api/inlineFile/**",
                        "/api/inlineUserImg/**",
                        "/api/getPostCountPerTagByUsername/**",
                        "/api/getPostByTagIdAndUsername",
                        "/api/getUserImg/**",
                        "/api/getUser/**",
                        "/api/getTagAutoCompleteList/**",
                        "/api/logout",
                        "/api/refresh",
                        "/api/sendEmailCode",
                        "/api/sendEmailCode/**",
                        "/api/deleteUser/**",
                        "/api/resetPassword",
                        "/api/resetPassword/**",
                        "/api/sendResetPwEmail/**",
                        "/api/getEmailByResetToken/{token}",
                        "/api/getEmailByResetToken/**",
                        "/api/getPostCountPerTag/**"
                    ).permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/api/ai/**").hasAnyAuthority("AI_USER", "ADMIN")
                    .requestMatchers("/api/thumbnail-jobs", "/api/thumbnail-jobs/**")
                        .hasAnyAuthority("AI_USER", "ADMIN")
                    .anyRequest().authenticated()
    //                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    //인증 실패 처리 (로그인 안한 상태에서 보호된 API 접근)
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                    response.getWriter().write("{\"error\": \"Unauthorized or Token expired\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    //인가 실패 처리 (권한이 부족한 요청)
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                    response.getWriter().write("{\"error\": \"Forbidden\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    // 커스턴 AuthenticationProvider 설정
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
        CustomAuthenticationProvider customAuthenticationProvider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .authenticationProvider(customAuthenticationProvider)
            .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
