package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateCategoryDto;
import com.example.productcatalogservice.entity.Category;
import com.example.productcatalogservice.exception.CategoryDuplicateException;
import com.example.productcatalogservice.exception.CategoryNotFoundException;
import com.example.productcatalogservice.repositoty.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_ShouldReturnSuccessCreatedCategory_WhenParentIsProvidedAndDuplicateDoesNotExist() {
        var name = "phone";
        var parentPublicId = UUID.randomUUID();

        var parentCategory = Category.builder()
                .id(1L)
                .publicId(parentPublicId)
                .name("technic")
                .build();
        var category = Category.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .name(name)
                .parent(parentCategory)
                .build();

        var createDto = new CreateCategoryDto(name, "phone category", parentPublicId);

        when(categoryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.createCategory(createDto);

        assertEquals(category, result);
    }

    @Test
    void createCategory_ShouldReturnCategoryNotFoundException_WhenParentIsProvidedAndParentNotFound() {
        var name = "phone";
        var parentPublicId = UUID.randomUUID();

        var parentCategory = Category.builder()
                .id(1L)
                .publicId(parentPublicId)
                .name("technic")
                .build();
        var category = Category.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .name(name)
                .parent(parentCategory)
                .build();

        var createDto = new CreateCategoryDto(name, "phone category", parentPublicId);

        when(categoryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.createCategory(createDto));

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldReturnCategoryDuplicateException_WhenDuplicateCategoryExist() {
        var name = "phone";

        var createDto = new CreateCategoryDto(name, "phone category", null);

        when(categoryRepository.save(any(Category.class))).thenThrow(new DataIntegrityViolationException(""));

        assertThrows(CategoryDuplicateException.class,
                () -> categoryService.createCategory(createDto));


        verify(categoryRepository, never()).findByPublicId(any(UUID.class));
    }

    @Test
    void getCategoryById_ShouldReturnCategoryById_WhenCategoryExist() {
        var id = 1L;
        var category = Category.builder()
                .id(1L)
                .name("phone")
                .build();

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(id);

        assertEquals(1, result.getId());
        assertEquals("phone", result.getName());
    }

    @Test
    void getCategoryById_ShouldReturnCategoryNotFoundException_WhenCategoryDoesNotExist() {
        var id = 1L;
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(id));
    }

    @Test
    void getCategoryByPublicId_ShouldReturnCategoryByPublicId_WhenCategoryExist() {
        var publicId = UUID.randomUUID();
        var category = Category.builder()
                .id(1L)
                .publicId(publicId)
                .name("phone")
                .build();

        when(categoryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryByPublicId(publicId);

        assertEquals(1L, result.getId());
        assertEquals(publicId, result.getPublicId());
    }

    @Test
    void getCategoryByPublicId_ShouldReturnCategoryNotFoundException_WhenCategoryDoesNotExist() {
        var publicId = UUID.randomUUID();

        when(categoryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategoryByPublicId(publicId));
    }


    @Test
    void getCategoryBySlug_ShouldReturnCategoryBySlug_WhenCategoryExist() {
        var slug = "phone";

        var category = Category.builder()
                .id(1L)
                .slug(slug)
                .name("Phone")
                .build();

        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryBySlug(slug);

        assertEquals(1L, result.getId());
        assertEquals(slug, result.getSlug());
    }

    @Test
    void getCategoryBySlug_ShouldReturnCategoryNotFoundException_WhenCategoryDoesNotExist() {
        var slug = "phone";

        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.getCategoryBySlug(slug));
    }

    @Test
    void getCategoriesByPublicIds_ShouldReturnDoesNotEmptyCollection_WhenCategoriesExists() {
        var publicId = UUID.randomUUID();
        var ids = List.of(publicId);
        var categories = List.of(Category.builder()
                        .id(1L)
                        .name("Phone")
                .build());

        when(categoryRepository.findByPublicIdIn(anyList())).thenReturn(categories);

        List<Category> result = categoryService.getCategoriesByPublicIds(ids);

        assertThat(result)
                .isNotEmpty()
                .hasSize(1);
    }

    @Test
    void getCategoriesByPublicIds_ShouldReturnEmptyCollection_WhenCategoriesDoesNotExists() {
        when(categoryRepository.findByPublicIdIn(anyList())).thenReturn(List.of());

        List<Category> result = categoryService.getCategoriesByPublicIds(List.of(UUID.randomUUID()));

        assertThat(result).isEmpty();
    }
}












