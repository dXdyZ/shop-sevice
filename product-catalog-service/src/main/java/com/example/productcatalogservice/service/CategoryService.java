package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.CreateCategoryDto;
import com.example.productcatalogservice.dto.PatentRef;
import com.example.productcatalogservice.entity.Category;
import com.example.productcatalogservice.exception.CategoryNotFoundException;
import com.example.productcatalogservice.repositoty.CategoryRepository;
import com.example.productcatalogservice.util.CategoryMapper;
import com.example.productcatalogservice.util.SlugMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(CreateCategoryDto createDto) {
        Category parentCategory = null;
        if (createDto.patentRef() != null) {
            PatentRef ref = createDto.patentRef();
            if (ref.publicId() != null) {
                parentCategory = categoryRepository.findByPublicId(ref.publicId()).orElseThrow(
                        () -> new CategoryNotFoundException("Parent category not found"));
            } else if (ref.id() != null) {
                parentCategory = categoryRepository.findById(ref.id()).orElseThrow(
                        () -> new CategoryNotFoundException("Parent category not found"));
            } else if (ref.slug() != null) {
                parentCategory = categoryRepository.findBySlug(ref.slug()).orElseThrow(
                        () -> new CategoryNotFoundException("Parent category not found"));
            }
        }
        Category category = CategoryMapper.fromDto(createDto);
        category.setSlug(SlugMapper.fromName(createDto.name()));
        if (parentCategory != null) {
            category.setParent(parentCategory);
        }
        return categoryRepository.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category not found"));
    }

    public Category getCategoryByPublicId(UUID publicId) {
        return categoryRepository.findByPublicId(publicId).orElseThrow(
                () -> new CategoryNotFoundException("Category not found"));
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug).orElseThrow(
                () -> new CategoryNotFoundException("Category not found"));
    }
}









