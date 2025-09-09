package com.shop.userservice.controller;

import com.shop.userservice.dto.ErrorResponse;
import com.shop.userservice.exception.ExternalServiceUnavailableException;
import com.shop.userservice.exception.UserDuplicateException;
import com.shop.userservice.exception.UserNotFoundException;
import com.shop.userservice.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleUserDuplicate(UserDuplicateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .messageCode("DUPLICATE_USER")
                        .httpCode(HttpStatus.CONFLICT.value())
                        .message(exception.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .messageCode("USER_NOT_FOUND")
                        .httpCode(HttpStatus.NOT_FOUND.value())
                        .message(exception.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceUnavailable(ExternalServiceUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .messageCode("SERVER_ERROR")
                        .httpCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(exception.getMessage())
                        .timestamp(Instant.now())
                        .build());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            fieldError -> fieldError.getDefaultMessage() != null
                                    ? fieldError.getDefaultMessage()
                                    : "Validation failed"
                    ));

        ErrorResponse errorResponse = ErrorResponse.validationError(errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validationError(exception.getErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String fieldName = getFieldName(violation.getPropertyPath());
            errors.put(fieldName, violation.getMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.validationError(errors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    private String getFieldName(Path propertyPath) {
        String pathString = propertyPath.toString();

        //Извлекает только имя поля из пути ("createUser.user.email" -> "email")
        if (pathString.contains(".")) {
            return pathString.substring(pathString.lastIndexOf('.') + 1);
        }

        return pathString;
    }
}
