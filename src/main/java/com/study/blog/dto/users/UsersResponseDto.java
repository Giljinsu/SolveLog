package com.study.blog.dto.users;

import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.entity.File;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class UsersResponseDto {
    private Long userId;
    private String nickname;
    private String bio;
    private FileResponseDto userImg;

    public UsersResponseDto(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    public UsersResponseDto(Long userId, String nickname, FileResponseDto userImg) {
        this.userId = userId;
        this.nickname = nickname;
        this.userImg = userImg;
    }

    public UsersResponseDto(Long userId, String nickname, String bio, FileResponseDto userImg) {
        this.userId = userId;
        this.nickname = nickname;
        this.bio = bio;
        this.userImg = userImg;
    }
}