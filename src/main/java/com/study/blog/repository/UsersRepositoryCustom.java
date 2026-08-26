package com.study.blog.repository;

import com.study.blog.admin.dto.AdminUserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsersRepositoryCustom {

    // 관리자 Users 목록 - keyword로 username(이메일 역할) / nickname 통합 검색
    Page<AdminUserResponseDto> findUsersForAdmin(String keyword, Pageable pageable);
}
