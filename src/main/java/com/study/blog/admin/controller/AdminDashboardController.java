package com.study.blog.admin.controller;

import com.study.blog.admin.dto.AdminDashboardResponseDto;
import com.study.blog.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 - SecurityConfig에서 /api/admin/** 는 ADMIN만 접근 가능
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<AdminDashboardResponseDto> getDashboard() {
        return ResponseEntity.ok(adminUserService.getDashboard());
    }
}
