package com.example.productcatalogservice.util;

import com.example.productcatalogservice.dto.create.CreateBrandDto;
import com.example.productcatalogservice.entity.Brand;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class BrandMapper {
    public static Brand fromCreateDto(CreateBrandDto createDto) {
        return Brand.builder()
                .name(createDto.name())
                .slug(SlugMapper.from(createDto.name()))
                .description(createDto.description())
                .build();
    }
}
