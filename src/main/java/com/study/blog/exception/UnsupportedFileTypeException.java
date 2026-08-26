package com.study.blog.exception;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException() {
        super("unsupported file type");
    }
}
