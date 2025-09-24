package com.study.blog.entity;

import com.study.blog.entity.basicentity.BasicDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class Post extends BasicDate {

    @Id @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;
    private int viewCount;
    private String tags;
    private String summary;
    private Boolean isTemp;

    public Post(String title, String content) {
        this.content = content;
        this.title = title;
    }

    //=== 연관관계 메서드 ===
    public void addUser(Users user) {
        this.user = user;
        user.addPost(this);
    }

    //== 생성 메서드
    public static Post createPost(String title, String content, Users user, Category category,
        String tags, String summary, Boolean isTemp) {

        Post newPost = new Post(title, content);
        newPost.setViewCount(0);
        newPost.addUser(user);
        newPost.setCategory(category);
        newPost.setTags(tags);
        newPost.setSummary(summary);
        newPost.setIsTemp(isTemp);

        return newPost;
    }

    public static Post createPost(String title, String content, Users user, Category category,
        String summary, Boolean isTemp) {

        Post newPost = new Post(title, content);
        newPost.setViewCount(0);
        newPost.addUser(user);
        newPost.setCategory(category);
        newPost.setSummary(summary);
        newPost.setIsTemp(isTemp);

        return newPost;
    }

    public void updatePost(String title, String content, Category category, String tags,
        String summary, Boolean isTemp) {
        setTitle(title);
        setContent(content);
        setCategory(category);
        setTags(tags);
        setSummary(summary);
        setIsTemp(isTemp);
    }

    public void setViews(int views) {
        setViewCount(views);
    }

}
