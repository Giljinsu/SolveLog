package com.study.blog.exception;

public class NotValidateEmailCode extends RuntimeException {

    public NotValidateEmailCode() {
        super("not validate email code");
    }
}
