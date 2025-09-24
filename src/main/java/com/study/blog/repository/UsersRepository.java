package com.study.blog.repository;

import com.study.blog.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Boolean existsByUsername(String username);

    Optional<Users> findUsersByUsername(String username);
}
