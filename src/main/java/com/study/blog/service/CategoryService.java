package com.study.blog.service;

import com.study.blog.dto.category.CategoryResponseDto;
import com.study.blog.entity.Category;
import com.study.blog.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getCategoryList(String parentType) {
        Category parentCategory = categoryRepository.findByType(parentType);

        return categoryRepository.findByParentCategoryIdOrderById(parentCategory.getId())
            .stream().map(category ->
                new CategoryResponseDto(category.getId(),
                    category.getParentCategory().getId(),
                    category.getType()))
            .toList();
    }
}
