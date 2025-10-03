package com.example.productcatalogservice.util.mapper;

import com.example.productcatalogservice.dto.create.CreateAttributeDto;
import com.example.productcatalogservice.entity.Attribute;

public final class AttributeMapper {
    public static Attribute fromCreateDto(CreateAttributeDto createDto) {
        return Attribute.builder()
                .name(createDto.name())
                .slug(SlugMapper.from(createDto.name()))
                .filterable(createDto.filterable())
                .build();

    }
}
