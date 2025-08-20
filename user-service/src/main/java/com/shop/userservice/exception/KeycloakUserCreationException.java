package com.shop.userservice.exception;

import lombok.Getter;

public class KeycloakUserCreationException extends RuntimeException {

    @Getter
    private final int status;

    public KeycloakUserCreationException(String message, int status) {
        super(message);
        this.status = status;
    }
}
