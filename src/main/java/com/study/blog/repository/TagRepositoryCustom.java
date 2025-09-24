package com.study.blog.repository;

import com.study.blog.dto.tag.TagResponseDto;
import java.util.List;

public interface TagRepositoryCustom {
    List<TagResponseDto> getTagAutoComplete(String value);
}
