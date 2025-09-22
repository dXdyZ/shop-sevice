package com.example.productcatalogservice.util;

import com.example.productcatalogservice.dto.CreateBrandDto;
import com.example.productcatalogservice.entity.Brand;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class BrandMapper {
    public static Brand fromDto(CreateBrandDto createDto) {
        return Brand.builder()
                .name(createDto.name())
                .description(createDto.description())
                .build();
    }
}
