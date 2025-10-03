package com.example.productcatalogservice.exception;

public class FeedbackDuplicateException extends RuntimeException {
    public FeedbackDuplicateException(String message) {
        super(message);
    }
}
