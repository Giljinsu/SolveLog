package com.study.blog.controller;

import com.study.blog.dto.tag.TagResponseDto;
import com.study.blog.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping("/api/getTagAutoCompleteList/{value}")
    public ResponseEntity<List<TagResponseDto>> getTagAutoCompleteList(@PathVariable String value) {
        return ResponseEntity.ok(tagService.getTagAutoComplete(value));
    }
}
