package com.study.blog.repository;

import com.study.blog.dto.postTag.TagCountDto;
import com.study.blog.dto.statistic.SolveStatisticResponseDto;
import com.study.blog.dto.statistic.UserCategoryStatisticDto;
import com.study.blog.dto.statistic.UserDailyStatisticDto;
import java.util.List;

public interface UserStatisticRepositoryCustom {

    SolveStatisticResponseDto getPostCountSummary(Long userId, String categoryType);
    List<UserDailyStatisticDto> getDailyPostCount(Long userId, String categoryType);
    List<TagCountDto> getTagPostCount(Long userId, String categoryType);
    List<UserCategoryStatisticDto> getCategoryPostCount(Long userId);


}
