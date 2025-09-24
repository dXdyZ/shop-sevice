package com.example.productcatalogservice.dto;

import java.util.List;
import java.util.UUID;

public record BrandDto(
        UUID publicId,
        String slug,
        String name,
        String description,
        List<ProductDto> products
) {}
