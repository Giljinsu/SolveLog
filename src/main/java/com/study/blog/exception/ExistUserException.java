package com.study.blog.exception;

public class ExistUserException extends RuntimeException {

    public ExistUserException() {
        super("exist username");
    }
}
