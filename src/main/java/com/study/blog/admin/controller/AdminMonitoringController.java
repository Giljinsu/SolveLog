package com.study.blog.admin.controller;

import com.study.blog.admin.dto.AdminErrorEventResponseDto;
import com.study.blog.admin.dto.AdminErrorIssueDetailResponseDto;
import com.study.blog.admin.dto.AdminErrorIssueResponseDto;
import com.study.blog.admin.service.AdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 관리자 전용 - SecurityConfig에서 /api/admin/** 는 ADMIN만 접근 가능
@RestController
@RequestMapping("/api/admin/monitoring")
@RequiredArgsConstructor
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    @GetMapping("/issues")
    public ResponseEntity<Page<AdminErrorIssueResponseDto>> getIssues(Pageable pageable) {
        return ResponseEntity.ok(adminMonitoringService.getIssues(pageable));
    }

    @GetMapping("/issues/{issueId}")
    public ResponseEntity<AdminErrorIssueDetailResponseDto> getIssueDetail(@PathVariable Long issueId) {
        return ResponseEntity.ok(adminMonitoringService.getIssueDetail(issueId));
    }

    @GetMapping("/issues/{issueId}/events")
    public ResponseEntity<Page<AdminErrorEventResponseDto>> getEvents(
        @PathVariable Long issueId, Pageable pageable) {
        return ResponseEntity.ok(adminMonitoringService.getEvents(issueId, pageable));
    }
}
