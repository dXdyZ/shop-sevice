package com.example.productcatalogservice.exception;

public class InventoryDuplicateException extends RuntimeException {
    public InventoryDuplicateException(String message) {
        super(message);
    }
}
