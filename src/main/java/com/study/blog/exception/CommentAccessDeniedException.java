package com.study.blog.exception;

public class CommentAccessDeniedException extends RuntimeException {

    public CommentAccessDeniedException() {
        super("no access to this comment");
    }
}
