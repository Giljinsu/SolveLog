package com.study.blog.exception;

public class BatchJobAlreadyRunningException extends RuntimeException {

    public BatchJobAlreadyRunningException(String message) {
        super(message);
    }
}
