package com.example.productcatalogservice.exception.elastic;

public class ProductDocumentNotFoundException extends RuntimeException {
    public ProductDocumentNotFoundException(String message) {
        super(message);
    }
}
