package com.study.blog.dto;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class ErrorResponse {
    private String code;
    private String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
