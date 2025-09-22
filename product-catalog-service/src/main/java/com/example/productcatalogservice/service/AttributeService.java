package com.example.productcatalogservice.service;

import com.example.productcatalogservice.repositoty.AttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttributeService {
    private final AttributeRepository attributeRepository;


}
