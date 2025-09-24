package com.study.blog.repository;

import com.study.blog.entity.Alarm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    Alarm getAlarmById(Long id);

    List<Alarm> getAlarmByUsers_Username(String users_username);

    @Query("select count(a) from Alarm a where a.users.username = :username and a.isViewed is false")
    Long getNotViewedAlarmCnt(@Param("username") String username);

    @Modifying(clearAutomatically = true)
    @Query("delete from Alarm a where a.users.username = :username")
    void bulkDeleteAlarmsByUsername(@Param("username") String username);

    @Modifying(clearAutomatically = true)
    @Query("update Alarm a set a.isViewed = true where a.users.username = :username and a.isViewed = false")
    void bulkUpdateAlarmIsViewTrue(@Param("username") String username);
}
