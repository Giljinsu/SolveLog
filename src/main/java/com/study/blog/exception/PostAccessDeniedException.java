package com.study.blog.exception;

public class PostAccessDeniedException extends RuntimeException {

    public PostAccessDeniedException() {
        super("no access to this post");
    }
}
