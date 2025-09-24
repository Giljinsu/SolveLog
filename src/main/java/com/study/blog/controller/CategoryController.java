package com.study.blog.controller;

import com.study.blog.dto.category.CategoryResponseDto;
import com.study.blog.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/api/getCategoryListByParentType/{categoryType}")
    public ResponseEntity<Result<List<CategoryResponseDto>>> getCategoryListByParentType(
        @PathVariable String categoryType) {
        return ResponseEntity.ok(Result.of(categoryService.getCategoryList(categoryType)));
    }
}
