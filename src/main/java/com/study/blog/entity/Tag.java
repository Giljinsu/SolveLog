package com.study.blog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id @GeneratedValue
    @Column(name = "tag_id")
    private Long id;

    @Column(unique = true)
    private String name;

    public Tag(String name) {
        this.name = name;
    }

    // 생성메서드
    public static Tag createTag(String name) {
        return new Tag(name);
    }
}
