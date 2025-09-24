package com.example.productcatalogservice.exception;

public class DuplicateAttributeException extends RuntimeException {
    public DuplicateAttributeException(String message) {
        super(message);
    }
}
