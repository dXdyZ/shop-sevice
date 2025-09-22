package com.example.productcatalogservice.dto;

import java.util.UUID;

public record CreateCategoryDto(
        String name,
        String description,
        PatentRef patentRef
) {}


