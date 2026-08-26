package com.study.blog.exception;

public class ThumbnailJobNotFoundException extends RuntimeException {

    public ThumbnailJobNotFoundException() {
        super("not found thumbnail job");
    }
}
