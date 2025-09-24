package com.study.blog.repository;

import static org.assertj.core.api.Assertions.*;

import com.study.blog.dto.category.CategoryResponseDto;
import com.study.blog.entity.Category;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class CategoryRepositoryTest {

    @Autowired CategoryRepository categoryRepository;
    @Autowired
    EntityManager em;

    @Test
    @Transactional
    public void searchByParentIdTest() {
        //category
        Category parentCategory = Category.createCategory("SEARCH_CATEGORY");
        Category solveCategory = Category.createCategory("문제풀이",parentCategory);
        Category freeCategory = Category.createCategory("자유게시판", parentCategory);
        Category freeCategory2 = Category.createCategory("test", parentCategory);

        categoryRepository.save(parentCategory);
        categoryRepository.save(solveCategory);
        categoryRepository.save(freeCategory);
        categoryRepository.save(freeCategory2);

        em.flush();
        em.clear();

        List<Category> childCategories = categoryRepository.findByParentCategoryIdOrderById(
            parentCategory.getId());

        assertThat(childCategories.size()).isEqualTo(3);

        childCategories.stream().map(
            category -> new CategoryResponseDto(category.getId(), category.getParentCategory().getId(), category.getType()));
    }
}