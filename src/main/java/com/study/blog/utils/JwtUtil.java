package com.study.blog.utils;


import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    // 시크릿 키는 반드시 충분히 길어야함 256비트 이상 권장
    private final String secret = "super-secret-key-for-jwt-should-be-long-enough";
    private Key key;

    private final long accessTokenValidity = 1000 * 60 * 30; // 30분
    private final long refreshTokenValidity = 1000 * 60 * 60 * 24 * 7; // 7일

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    //Access Token 생성
    public String generateAccessToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    //Refresh Token 생성
    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    //토큰에서 사용자명 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

    //요청에서 토큰 추출
    public String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // 클라이언트 : Authorization: Bearer <refreshToken>
        if (authHeader == null || !authHeader.equals("Bearer ")) {
            return null;
        }

        return authHeader.substring(7); // "Bearer " 이후
    }

    //유효성 검사
    public boolean isTokenValid(String token) {
        // 서명 위조, 만료, 구조 오류 등 모든 비정상 토큰은 false 리턴
        // 토큰이 유효한지 체크 키가 맞는지 혹은 만료되었는지 등
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalStateException e) {
            return false;
        }

    }

}
