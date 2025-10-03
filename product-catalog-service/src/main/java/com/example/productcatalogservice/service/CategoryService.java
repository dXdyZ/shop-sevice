package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateCategoryDto;
import com.example.productcatalogservice.entity.Category;
import com.example.productcatalogservice.exception.CategoryDuplicateException;
import com.example.productcatalogservice.exception.CategoryNotFoundException;
import com.example.productcatalogservice.repositoty.CategoryRepository;
import com.example.productcatalogservice.util.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(CreateCategoryDto createDto) {
        Category parentCategory = null;
        if (createDto.parentPublicId() != null) {
            parentCategory = categoryRepository.findByPublicId(createDto.parentPublicId()).orElseThrow(
                    () -> new CategoryNotFoundException("Parent category not found"));
        }

        Category category = CategoryMapper.fromCreateDto(createDto);
        category.setParent(parentCategory);

        try {
            return categoryRepository.save(category);
        } catch (DataIntegrityViolationException exception) {
            throw new CategoryDuplicateException("Category by name: %s already exist".formatted(createDto.name()));
        }
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

    public List<Category> getCategoriesByPublicIds(List<UUID> publicIds) {
        return categoryRepository.findByPublicIdIn(publicIds);
    }
}









