package com.study.blog.admin.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class AdminDashboardResponseDto {
    private final long totalUserCount;
    private final long userCount;
    private final long aiUserCount;
    private final long adminCount;
    private final long totalPostCount;
    private final List<AdminUserResponseDto> recentUsers;

    public AdminDashboardResponseDto(long totalUserCount, long userCount, long aiUserCount,
        long adminCount, long totalPostCount, List<AdminUserResponseDto> recentUsers) {
        this.totalUserCount = totalUserCount;
        this.userCount = userCount;
        this.aiUserCount = aiUserCount;
        this.adminCount = adminCount;
        this.totalPostCount = totalPostCount;
        this.recentUsers = recentUsers;
    }
}
