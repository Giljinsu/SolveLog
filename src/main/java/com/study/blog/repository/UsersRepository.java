package com.study.blog.repository;

import com.study.blog.entity.Users;
import com.study.blog.entity.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsersRepository extends JpaRepository<Users, Long>, UsersRepositoryCustom {

    @Query("select case when count(u) > 0 then true else false end "
        + "from Users u "
        + "where u.isDeleted = 'n' and u.username = :username ")
    Boolean existsByUsername(String username);

    Optional<Users> findUsersByUsername(String username);

    @Query("select u "
        + "from Users u "
        + "where u.isDeleted = 'n' and u.username = :username ")
    Optional<Users> findUsersByUsernameIsNotDeleted(String username);

    // 관리자 Dashboard - 전체 사용자 수 (탈퇴 제외)
    @Query("select count(u) from Users u where u.isDeleted = 'n'")
    long countByIsDeletedFalse();

    // 관리자 Dashboard - Role별 사용자 수 (탈퇴 제외)
    @Query("select count(u) from Users u where u.isDeleted = 'n' and u.role = :role")
    long countByRole(@Param("role") Role role);

    // 관리자 Dashboard - 최근 가입 사용자
    @Query("select u from Users u where u.isDeleted = 'n' order by u.createdDate desc")
    List<Users> findRecentUsers(Pageable pageable);
}

