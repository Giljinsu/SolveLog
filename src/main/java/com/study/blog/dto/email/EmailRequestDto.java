package com.study.blog.dto.email;

import lombok.Data;

@Data
public class EmailRequestDto {
    private String email;
    private String authCode;
    private String title;
    private String content;

    public EmailRequestDto(String email, String authCode) {
        this.email = email;
        this.authCode = authCode;
    }
}
