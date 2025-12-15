package com.study.blog.dto.users;

import lombok.Data;

@Data
public class ResetTokenDto {
    private String resetToken;
    private String username;
    private String password;
}
