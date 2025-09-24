package com.study.blog.service;

import static org.assertj.core.api.Assertions.*;

import com.study.blog.controller.LoginController_JwtAuth;
import com.study.blog.dto.login.LoginRequestDto;
import com.study.blog.dto.users.UserRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class LoginServiceTest {
    @Autowired UsersService usersService;
    @Autowired
    LoginController_JwtAuth loginController;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired RefreshTokenService refreshTokenService;

    @Test
    public void storeTokenRedisTest() throws InterruptedException {
        String userKey = "rt:user:";

        UserRequestDto userRequestDto = new UserRequestDto("test", "1234", "helloWorld");
        usersService.createUser(userRequestDto);

        LoginRequestDto loginRequestDto = new LoginRequestDto(userRequestDto.getUsername(),
            userRequestDto.getPassword());
        loginController.login(loginRequestDto);

        assertThat(stringRedisTemplate.hasKey(userKey+loginRequestDto.getUsername())).isTrue();

        String refreshToken = stringRedisTemplate.opsForValue()
            .get(userKey + loginRequestDto.getUsername());

        assertThat(refreshTokenService.validateToken(loginRequestDto.getUsername(), refreshToken))
            .isTrue();

        refreshTokenService.deleteToken(loginRequestDto.getUsername());

        // 만료 테스트
//        Thread.sleep(4004);
//
        assertThat(refreshTokenService.validateToken(loginRequestDto.getUsername(), refreshToken))
            .isFalse();


    }
}

