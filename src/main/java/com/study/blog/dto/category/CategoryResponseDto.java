package com.study.blog.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryResponseDto {
    private Long categoryId;
    private Long parentCategoryId;
    private String type;

    public CategoryResponseDto(Long categoryId, Long parentCategoryId, String type) {
        this.categoryId = categoryId;
        this.parentCategoryId = parentCategoryId;
        this.type = type;
    }
}
