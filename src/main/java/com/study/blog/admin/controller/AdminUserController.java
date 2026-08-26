package com.study.blog.admin.controller;

import com.study.blog.admin.dto.AdminRoleUpdateRequestDto;
import com.study.blog.admin.dto.AdminUserResponseDto;
import com.study.blog.admin.service.AdminUserService;
import com.study.blog.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 - SecurityConfig에서 /api/admin/** 는 ADMIN만 접근 가능
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDto>> getUsers(
        @RequestParam(required = false) String keyword,
        Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(keyword, pageable));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<AdminUserResponseDto> updateRole(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long userId,
        @Valid @RequestBody AdminRoleUpdateRequestDto requestDto) {
        return ResponseEntity.ok(
            adminUserService.changeRole(userId, requestDto.getRole(), userDetails.getUserId()));
    }
}
