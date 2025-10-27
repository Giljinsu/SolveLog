package com.study.blog.entity;

import com.study.blog.entity.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class Users {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String username; // 로그인 아이디

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    private Long userImgId;

    private String bio;

    private Users(String username, String password,
        String nickname, Role role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    //생성 메서드
    public static Users createUser(String username, String password, String nickname, Role role) {
        return new Users(username, password, nickname, role);
    }

    public void updateUser(String nickname, Role role) {
        setNickname(nickname);
        setRole(role);
    }

    public void updateUser(String nickname, String bio, Role role) {
        setNickname(nickname);
        setBio(bio);
        setRole(role);
    }

    public void uploadUserImg(Long fileId) {
        this.userImgId = fileId;
    }

    //=== 연관관계 메서드 ===
    protected void addPost(Post post) {
        this.posts.add(post);
    }

    protected void addComment(Comment comment) {
        this.comments.add(comment);
    }



    // 사전 처리
    @PrePersist
    public void setCreatedDate() {
        this.createdDate = LocalDateTime.now();
    }


}
