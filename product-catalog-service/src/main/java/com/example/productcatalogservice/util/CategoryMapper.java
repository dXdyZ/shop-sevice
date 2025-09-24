package com.example.productcatalogservice.util;

import com.example.productcatalogservice.dto.create.CreateCategoryDto;
import com.example.productcatalogservice.entity.Category;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class CategoryMapper {
    public static Category fromCreateDto(CreateCategoryDto createDto) {
        return Category.builder()
                .name(createDto.name())
                .slug(SlugMapper.from(createDto.name()))
                .description(createDto.description())
                .build();
    }
}
