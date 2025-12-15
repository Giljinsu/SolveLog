package com.study.blog.dto.users;

import com.study.blog.entity.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter
@NoArgsConstructor
public class UserRequestDto {
    private Long userId;
    private String username;
    private String password;
    private String nickName;
    private String bio;
    private Role role;
    private String authCode;

    public UserRequestDto(String username, String password, String nickName) {
        this.username = username;
        this.password = password;
        this.nickName = nickName;
    }

    public UserRequestDto(Long userId, String password, String nickName) {
        this.userId = userId;
        this.password = password;
        this.nickName = nickName;
    }
}
