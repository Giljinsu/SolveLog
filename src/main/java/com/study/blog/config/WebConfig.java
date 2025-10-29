package com.study.blog.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    @Profile("prod")
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 경로
//                    .allowedOrigins("http://localhost:5173") // 리액트 서버 주소
                    .allowedOrigins("https://www.solvelog.site")
                    .allowedMethods("*") // GET, POST, PUT, DELETE 등
                    .allowedHeaders("*")
                    .allowCredentials(true); //withCredentials: true 쓸 뎡우
            }
        };
    }

    @Bean   
    @Profile("local")
    public WebMvcConfigurer corsLocalConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 모든 경로
                    .allowedOrigins("http://localhost:5173") // 리액트 서버 주소
                    .allowedMethods("*") // GET, POST, PUT, DELETE 등
                    .allowedHeaders("*")
                    .allowCredentials(true); //withCredentials: true 쓸 뎡우
            }
        };
    }
}