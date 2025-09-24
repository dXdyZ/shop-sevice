package com.example.productcatalogservice.dto.create;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateProductDto(
        String name,
        UUID brandPublicId,
        UUID primaryCategoryPublicId,
        List<UUID> categoryPublicIds,
        String description,
        String longDescription,
        BigDecimal basePrice,
        String currency,
        Double weightKg,
        Double lengthCm,
        Double widthCm,
        Double heightCm,
        List<CreateCustomAttributeDto> customAttributes,
        Integer quantity,
        Integer lowStockThreshold
) {}
