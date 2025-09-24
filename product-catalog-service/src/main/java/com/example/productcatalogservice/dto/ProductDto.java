package com.example.productcatalogservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(
        UUID publicId,
        String name,
        BrandDto brand,
        String description,
        String longDescription,
        BigDecimal basePrice,
        String currency,
        Double weightKg,
        Double lengthCm,
        Double widthCm,
        Double heightCm,

) {}
