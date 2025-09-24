package com.example.productcatalogservice.exception;

public class BrandDuplicateException extends RuntimeException {
    public BrandDuplicateException(String message) {
        super(message);
    }
}
