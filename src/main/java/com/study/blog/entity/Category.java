package com.study.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class Category {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    private List<Category> childCategories = new ArrayList<>();

    @Column(nullable = false, length = 50, unique = true)
    private String type;

    private Category(String type) {
        this.type = type;
    }

    //== 연관관계 메서드
    private void addParentCategory(Category parentCategory) {
        setParentCategory(parentCategory);
        parentCategory.addChildCategory(this);
    }

    private void addChildCategory(Category category) {
        getChildCategories().add(category);
    }

    public static Category createCategory(String type) {
        return new Category(type);
    }

    public static Category createCategory(String type, Category parentCategory) {
        Category newCategory = new Category(type);
        newCategory.addParentCategory(parentCategory);

        return newCategory;
    }

}
