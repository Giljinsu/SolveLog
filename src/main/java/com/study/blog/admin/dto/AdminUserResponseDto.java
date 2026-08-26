package com.study.blog.admin.dto;

import com.study.blog.entity.enums.Role;
import java.time.LocalDateTime;
import lombok.Getter;

// 관리자 Users 목록 / Dashboard 최근 가입 사용자 공용 응답
// username이 곧 가입 시 사용한 이메일이므로 별도 email 필드를 두지 않는다.
@Getter
public class AdminUserResponseDto {
    private final Long userId;
    private final String username;
    private final String nickname;
    private final Role role;
    private final LocalDateTime createdAt;

    public AdminUserResponseDto(Long userId, String username, String nickname, Role role,
        LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.createdAt = createdAt;
    }
}
