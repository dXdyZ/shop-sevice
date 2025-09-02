package com.shop.userservice.exception;

public class IternalServerError extends RuntimeException {
    public IternalServerError(String message) {
        super(message);
    }
}
