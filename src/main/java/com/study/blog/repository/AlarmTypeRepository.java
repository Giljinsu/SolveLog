package com.study.blog.repository;

import com.study.blog.entity.AlarmType;
import com.study.blog.entity.enums.AlarmTypeEnum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmTypeRepository extends JpaRepository<AlarmType, Long> {

    List<AlarmType> findByType(AlarmTypeEnum type);
}
