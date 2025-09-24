package com.study.blog.config;

import com.study.blog.service.CustomAuthenticationProvider;
import com.study.blog.service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
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
                        "/api/login",
                        "/api/signup",
                        "/api/createUser",
                        "/api/hello",
                        "/api/getPostList",
                        "/api/getPostDetail/**", // /** 하위경로
                        "/api//getComments/**",
                        "/api/posts/**",
                        "/api/getCategoryListByParentType/**",
                        "/api/inlineFile/**",
                        "/api/inlineUserImg/**",
                        "/api/getPostCountPerTagByUsername/**",
                        "/api/getPostByTagIdAndUsername",
                        "/api/getUserImg/**",
                        "/api/getUser/**",
                        "/api/getTagAutoCompleteList/**"
                    ).permitAll()
                    .anyRequest().authenticated()
    //                .anyRequest().permitAll()
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
