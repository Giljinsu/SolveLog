package com.study.blog;

import com.study.blog.dto.ErrorResponse;
import com.study.blog.exception.ExistUserException;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.exception.NotValidateEmailCode;
import com.study.blog.exception.NotValidateResetToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExistUserException.class)
    public ResponseEntity<?> handleExistUserException(ExistUserException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT) // 409 Conflict
            .body(new ErrorResponse("DUPLICATE_USERNAME", "이미 사용 중인 아이디입니다."));
    }

    //NotExistUserException

    //LockedException
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLockedException(LockedException e) {
        return ResponseEntity
            .status(HttpStatus.LOCKED) // 423
            .body(new ErrorResponse("LOCKED_USER", "잠긴 계정입니다 10분후 재시도 해주세요."));
    }

    //NotExistUserException
    @ExceptionHandler(NotExistUserException.class)
    public ResponseEntity<?> handleNotExistUserException(NotExistUserException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_EXIST_USER", "이메일이 존재하지 않습니다."));
    }

    //NotValidateEmailCode
    @ExceptionHandler(NotValidateEmailCode.class)
    public ResponseEntity<?> handleNotValidateEmailCode(NotValidateEmailCode e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_VALIDATE_EMAIL_CODE", "유효하지 않은 인증 코드입니다."));
    }

    //NotValidateResetToken
    @ExceptionHandler(NotValidateResetToken.class)
    public ResponseEntity<?> handleNotValidateResetToken(NotValidateResetToken e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_VALIDATE_RESET_TOKEN", "유효하지 않은 토큰입니다."));
    }



}
