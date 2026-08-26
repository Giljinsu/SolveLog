package com.study.blog.exception;

public class PostNotFoundException extends RuntimeException {

    public PostNotFoundException() {
        super("not found post");
    }
}
