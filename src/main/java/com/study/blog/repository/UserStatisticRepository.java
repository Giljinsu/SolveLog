package com.study.blog.repository;

import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.entity.UserStatistic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserStatisticRepository
    extends JpaRepository<UserStatistic, Long>, UserStatisticRepositoryCustom {

}
