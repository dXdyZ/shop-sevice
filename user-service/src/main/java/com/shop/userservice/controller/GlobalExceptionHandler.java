package com.shop.userservice.controller;

import com.shop.userservice.dto.ErrorResponse;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleUserDuplicate(UserDuplicateException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .messageCode("DUPLICATE_USER")
                        .httpCode(HttpStatus.BAD_REQUEST.value())
                        .message(exception.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .messageCode("USER_NOT_FOUND")
                        .httpCode(HttpStatus.NOT_FOUND.value())
                        .message(exception.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }
}
