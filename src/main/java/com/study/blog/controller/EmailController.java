package com.study.blog.controller;

import com.study.blog.dto.email.EmailRequestDto;
import com.study.blog.dto.users.ResetTokenDto;
import com.study.blog.dto.users.UsersResponseDto;
import com.study.blog.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

//    @PostMapping("/api/sendEmailCode")
//    public void sendEmailCode(@RequestBody EmailRequestDto emailRequestDto) {
//        emailService.createAuthEmail(emailRequestDto);
//    }

    @PostMapping("/api/sendEmailCode")
    public ResponseEntity<Void> sendEmailCode(@RequestBody EmailRequestDto emailRequestDto,
        HttpServletRequest request) {
        System.out.println("URI = " + request.getRequestURI());
        emailService.createAuthEmail(emailRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/sendResetPwEmail/{username}")
    public ResponseEntity<Void> sendResetPwEmail(@PathVariable  String username) {
        emailService.sendResetPasswordEmail(username);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/api/validateEmailCode")
    public void validateEmailCode(@RequestBody EmailRequestDto emailRequestDto) {
        emailService.validateEmailCode(emailRequestDto);
    }

    @PostMapping("/api/getEmailByResetToken/{token}")
    public ResponseEntity<Result<ResetTokenDto>> getEmailByResetToken(@PathVariable String token) {
        ResetTokenDto resetTokenDto = new ResetTokenDto();
        resetTokenDto.setUsername(emailService.getEmailByToken(token));
        return ResponseEntity.ok(Result.single(resetTokenDto));
    }

//    @PostMapping("/api/getEmailByResetToken/{token}")
//    public Result<ResetTokenDto> getEmailByResetToken(@PathVariable String token) {
//        ResetTokenDto resetTokenDto = new ResetTokenDto();
//        resetTokenDto.setUsername(emailService.getEmailByToken(token));
//        System.out.println(resetTokenDto);
//        return Result.single(resetTokenDto);
//    }
}
