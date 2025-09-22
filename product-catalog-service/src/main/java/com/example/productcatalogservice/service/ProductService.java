package com.example.productcatalogservice.service;

import com.example.productcatalogservice.repositoty.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public void createProduct() {

    }
}
