package com.study.blog.controller;

import com.study.blog.dto.users.ResetTokenDto;
import com.study.blog.dto.users.UserRequestDto;
import com.study.blog.dto.users.UsersResponseDto;
import com.study.blog.service.UsersService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UsersService usersService;

    @GetMapping("/api/getUserList")
    public Result<List<UsersResponseDto>> getUserList() {
        return Result.of(usersService.getList());
    }

    @GetMapping("/api/getUser/{username}")
    public Result<UsersResponseDto> getUser(@PathVariable String username) {
        return Result.single(usersService.getOne(username));
    }

    @PostMapping("/api/createUser")
    public ResponseEntity<Void> createUser(@RequestBody UserRequestDto userRequestDto) {
        usersService.createUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //role 과 nickname 수정 가능
    @PostMapping("/api/updateUser")
    public ResponseEntity<Void> updateUser(@RequestBody UserRequestDto userRequestDto) {
        usersService.updateUser(userRequestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/api/deleteUser/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        usersService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/resetPassword")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetTokenDto resetTokenDto) {
        usersService.resetUserPassword(resetTokenDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
