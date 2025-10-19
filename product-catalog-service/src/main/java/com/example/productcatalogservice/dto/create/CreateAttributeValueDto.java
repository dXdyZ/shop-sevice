package com.example.productcatalogservice.dto.create;

import java.util.UUID;

public record CreateAttributeValueDto(
        String value,
        UUID publicId
){}
