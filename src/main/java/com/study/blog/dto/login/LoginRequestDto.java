package com.study.blog.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
@AllArgsConstructor
public class LoginRequestDto {
    private String username; // 로그인 아이아이
    private String password;
}
