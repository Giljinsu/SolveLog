package com.study.blog.admin.service;

import com.study.blog.admin.dto.AdminDashboardResponseDto;
import com.study.blog.admin.dto.AdminUserResponseDto;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.Role;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.exception.RoleChangeNotAllowedException;
import com.study.blog.repository.PostRepository;
import com.study.blog.repository.UsersRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final int RECENT_USER_COUNT = 5;

    private final UsersRepository usersRepository;
    private final PostRepository postRepository;

    // 관리자 Users 목록 - keyword로 username/nickname 통합 검색 + Pagination
    public Page<AdminUserResponseDto> getUsers(String keyword, Pageable pageable) {
        return usersRepository.findUsersForAdmin(keyword, pageable);
    }

    // 관리자 Dashboard
    public AdminDashboardResponseDto getDashboard() {
        long totalUserCount = usersRepository.countByIsDeletedFalse();
        long userCount = usersRepository.countByRole(Role.USER);
        long aiUserCount = usersRepository.countByRole(Role.AI_USER);
        long adminCount = usersRepository.countByRole(Role.ADMIN);
        long totalPostCount = postRepository.countByIsTempFalse();

        List<Users> recentUsers = usersRepository.findRecentUsers(
            PageRequest.of(0, RECENT_USER_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));

        List<AdminUserResponseDto> recentUserDtos = recentUsers.stream()
            .map(user -> new AdminUserResponseDto(
                user.getId(), user.getUsername(), user.getNickname(), user.getRole(),
                user.getCreatedDate()))
            .toList();

        return new AdminDashboardResponseDto(totalUserCount, userCount, aiUserCount, adminCount,
            totalPostCount, recentUserDtos);
    }

    // 관리자 Role 변경 - 자기 자신 변경 방지 + 최소 1명 ADMIN 유지
    @Transactional
    public AdminUserResponseDto changeRole(Long targetUserId, Role newRole, Long currentAdminUserId) {
        Users targetUser = usersRepository.findById(targetUserId)
            .orElseThrow(NotExistUserException::new);

        if (targetUser.getId().equals(currentAdminUserId)) {
            throw new RoleChangeNotAllowedException("자기 자신의 권한은 변경할 수 없습니다.");
        }

        if (targetUser.getRole() == Role.ADMIN && newRole != Role.ADMIN
            && usersRepository.countByRole(Role.ADMIN) <= 1) {
            throw new RoleChangeNotAllowedException("최소 1명의 관리자가 유지되어야 합니다.");
        }

        targetUser.updateUser(targetUser.getNickname(), targetUser.getBio(), newRole);

        return new AdminUserResponseDto(targetUser.getId(), targetUser.getUsername(),
            targetUser.getNickname(), targetUser.getRole(), targetUser.getCreatedDate());
    }
}
