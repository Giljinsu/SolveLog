package com.study.blog.exception;

public class NotValidateResetToken extends RuntimeException {

    public NotValidateResetToken() {
        super("not validate resetToken");
    }
}
