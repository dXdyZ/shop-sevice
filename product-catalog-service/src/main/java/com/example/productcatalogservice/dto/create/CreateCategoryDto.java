package com.example.productcatalogservice.dto.create;

import com.example.productcatalogservice.dto.PatentRef;

public record CreateCategoryDto(
        String name,
        String description,
        PatentRef patentRef
) {}


