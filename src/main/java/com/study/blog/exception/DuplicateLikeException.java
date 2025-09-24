package com.study.blog.exception;

public class DuplicateLikeException extends RuntimeException{

    public DuplicateLikeException() {
        super("already has Like");
    }
}
