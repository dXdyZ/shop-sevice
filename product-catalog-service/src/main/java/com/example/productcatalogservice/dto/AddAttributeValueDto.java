package com.example.productcatalogservice.dto;

import java.util.UUID;

public record AddAttributeValueDto (
        String value,
        UUID publicId
){}
