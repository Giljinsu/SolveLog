package com.study.blog.controller;

import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.dto.login.LoginRequestDto;
import com.study.blog.dto.login.LoginResponseDto;
import com.study.blog.exception.InvalidTokenException;
import com.study.blog.service.CustomUserDetails;
import com.study.blog.service.FileService;
import com.study.blog.service.RefreshTokenService;
import com.study.blog.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class LoginController_JwtAuth {
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final FileService fileService;


    @PostMapping("/api/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto requestDto) {
        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        // 1. 인증 시도 (Spring Security 가 알아서 유저 조회 + 비번 체크)
        Authentication authenticate = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                username,
                password
            )
        );

        String accessToken = jwtUtil.generateAccessToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);

        refreshTokenService.saveToken(username, refreshToken);

        return ResponseEntity.ok(new LoginResponseDto(accessToken, refreshToken));

    }

    @PostMapping("/api/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request) {
        String refreshToken = jwtUtil.extractTokenFromHeader(request);
        String username = null;

        try {
            username = jwtUtil.getUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidTokenException("잘못된 토큰 형식");
        }


        if (!jwtUtil.isTokenValid(refreshToken)) {
            refreshTokenService.deleteToken(username);
            throw new InvalidTokenException("리프레스 토큰 만료");
        }

//        String username = jwtUtil.getUsername(refreshToken);

        if (!refreshTokenService.validateToken(username, refreshToken)) {
            throw new InvalidTokenException("위조된 리프레스 토큰");
        }

        refreshTokenService.deleteToken(username); // 토큰 삭제

        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        refreshTokenService.saveToken(username, newRefreshToken);

        return ResponseEntity.ok(new LoginResponseDto(newAccessToken, newRefreshToken));
    }

    @GetMapping("/api/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않음");
        }

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        FileResponseDto userImg = fileService.findUserImgByUsername(
            customUserDetails.getUsername());

        return ResponseEntity.ok(Map.of(
            "username", customUserDetails.getUsername(),
            "roles", customUserDetails.getAuthorities(),
            "nickname", customUserDetails.getNickname(),
            "userImgId", userImg != null ? userImg.getFileId() : ""
        ));
    }

    @PostMapping("/api/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        refreshTokenService.deleteToken(userDetails.getUsername());
        return ResponseEntity.ok().body(Map.of("message", "로그아웃 성공"));

    }
}
