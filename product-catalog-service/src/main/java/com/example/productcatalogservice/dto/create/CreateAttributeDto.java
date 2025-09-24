package com.example.productcatalogservice.dto.create;

public record CreateAttributeDto (
        String name,
        String value,
        Boolean filterable
){}
