package com.study.blog;

import com.study.blog.dto.ErrorResponse;
import com.study.blog.exception.ExistUserException;
import com.study.blog.exception.NotExistUserException;
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
            .status(HttpStatus.LOCKED)
            .body(new ErrorResponse("LOCKED_USER", "잠긴 계정입니다 10분후 재시도 해주세요."));
    }

    //NotExistUserException
    @ExceptionHandler(NotExistUserException.class)
    public ResponseEntity<?> handleNotExistUserException(NotExistUserException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) // 404
            .body(new ErrorResponse("NOT_EXIST_USER", "아이디가 존재하지 않습니다."));
    }



}
