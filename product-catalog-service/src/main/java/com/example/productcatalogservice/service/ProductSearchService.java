package com.example.productcatalogservice.service;

import com.example.productcatalogservice.repositoty.elastic.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {
    private final ProductSearchRepository productSearchRepository;


}
