package com.study.blog.exception;

public class CommentNotFoundException extends RuntimeException {

    public CommentNotFoundException() {
        super("not found comment");
    }
}
