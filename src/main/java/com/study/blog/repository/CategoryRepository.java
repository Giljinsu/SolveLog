package com.study.blog.repository;

import com.study.blog.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c where c.type = :type")
    Category findByType(@Param("type") String type);


    List<Category> findByParentCategoryIdOrderById(Long parentCategoryId);
}
