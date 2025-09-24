package com.example.productcatalogservice.exception;

public class DuplicateAttributeValueException extends RuntimeException {
    public DuplicateAttributeValueException(String message) {
        super(message);
    }
}
