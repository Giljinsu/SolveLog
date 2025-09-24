package com.study.blog.entity;

import com.study.blog.entity.basicentity.BasicDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
public class Comment extends BasicDate {
    @Id @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment")
    private List<Comment> childComments = new ArrayList<>();

    private String comment;

    private Comment(String comment) {
        this.comment = comment;
    }

    //== 연관관계 메서드 ==
    public void addUser(Users user) {
        setUser(user);
        user.addComment(this);
    }

    public void addParentComment(Comment parentComment) {
        setParentComment(parentComment);
        parentComment.childComments.add(this);
    }

    // 생성 메서드
    public static Comment createComment(String comment, Users user, Post post) {
        Comment newComment = new Comment(comment);
        newComment.addUser(user);
        newComment.setPost(post);

        return newComment;
    }

    public static Comment createComment(String comment, Users user, Post post, Comment parentComment) {
        Comment newComment = createComment(comment, user, post);
        newComment.addParentComment(parentComment);

        return newComment;
    }

    public void updateComment(String comment) {
        setComment(comment);
    }

}
