package com.study.blog.exception;

public class NotExistUserException extends RuntimeException {

    public NotExistUserException() {
        super("not found username");
    }
}
