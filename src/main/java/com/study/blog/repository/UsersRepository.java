package com.study.blog.repository;

import com.study.blog.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsersRepository extends JpaRepository<Users, Long> {

    @Query("select case when count(u) > 0 then true else false end "
        + "from Users u "
        + "where u.isDeleted = 'n' and u.username = :username ")
    Boolean existsByUsername(String username);

    Optional<Users> findUsersByUsername(String username);

    @Query("select u "
        + "from Users u "
        + "where u.isDeleted = 'n' and u.username = :username ")
    Optional<Users> findUsersByUsernameIsNotDeleted(String username);
}

