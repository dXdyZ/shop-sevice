package com.shop.userservice.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation error");
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("ValidationException must contain at least 1 error");
        }
        this.errors = Collections.unmodifiableMap(errors);
    }
}
