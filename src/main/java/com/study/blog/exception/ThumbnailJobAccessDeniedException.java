package com.study.blog.exception;

public class ThumbnailJobAccessDeniedException extends RuntimeException {

    public ThumbnailJobAccessDeniedException() {
        super("no access to this thumbnail job");
    }
}
