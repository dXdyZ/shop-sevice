package com.example.productcatalogservice.util;

import com.example.productcatalogservice.dto.CreateCategoryDto;
import com.example.productcatalogservice.entity.Category;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CategoryMapper {
    public static Category fromDto(CreateCategoryDto createDto) {
        return Category.builder()
                .name(createDto.name())
                .description(createDto.description())
                .build();
    }
}
