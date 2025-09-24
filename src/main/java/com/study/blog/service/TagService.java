package com.study.blog.service;

import com.study.blog.dto.tag.TagResponseDto;
import com.study.blog.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    // 태그 자동완성 검색
    public List<TagResponseDto> getTagAutoComplete(String value) {
        return tagRepository.getTagAutoComplete(value);
    }
}
