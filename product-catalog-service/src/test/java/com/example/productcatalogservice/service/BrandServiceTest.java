package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateBrandDto;
import com.example.productcatalogservice.entity.Brand;
import com.example.productcatalogservice.exception.BrandDuplicateException;
import com.example.productcatalogservice.exception.BrandNotFoundException;
import com.example.productcatalogservice.repositoty.jpa.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {
    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void createBrand_ShouldReturnSuccessCreatedBrand_WhenDuplicateBrandDoestNotExist() {
        var name = "New Brand";
        var desc = "Brand for test";
        var createDto = new CreateBrandDto(name, desc);

        Brand brand = Brand.builder()
                .id(1L)
                .name(name)
                .description(desc)
                .build();

        when(brandRepository.save(any(Brand.class))).thenReturn(brand);

        Brand result = brandService.createBrand(createDto);

        assertEquals(result, brand);
    }

    @Test
    void createBrand_ShouldReturnBrandDuplicateException_WhenDuplicateBrandExist() {
        var name = "New Brand";
        var desc = "Brand for test";
        var createDto = new CreateBrandDto(name, desc);

        when(brandRepository.save(any(Brand.class))).thenThrow(new DataIntegrityViolationException("violation of restrictions"));

        assertThrows(BrandDuplicateException.class,
                () -> brandService.createBrand(createDto));
    }

    @Test
    void getBrandById_ShouldReturnBrandById_WhenBrandExist() {
        var id = 1L;
        Brand brand = Brand.builder()
                .id(1L)
                .build();

        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(brand));

        Brand result = brandService.getBrandById(id);

        assertEquals(1, result.getId());
    }

    @Test
    void getBrandById_ShouldReturnBrandNotFoundException_WhenBrandDoestNotExist() {
        var id = 1L;

        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrandById(id));
    }

    @Test
    void getBrandByPublicId_ShouldReturnBrandByPublicId_WhenBrandExist() {
        var publicId = UUID.randomUUID();
        Brand brand = Brand.builder()
                .id(1L)
                .publicId(publicId)
                .build();

        when(brandRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(brand));

        Brand result = brandService.getBrandByPublicId(publicId);

        assertEquals(brand, result);
    }

    @Test
    void getBrandByPublicId_ShouldReturnBrandNotFoundException_WhenBrandDoesNotExist() {
        var publicId = UUID.randomUUID();

        when(brandRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrandByPublicId(publicId));
    }

    @Test
    void getBrandBySlug_ShouldReturnBrandBySlug_WhenBrandExist() {
        var slug = "brand";
        Brand brand = Brand.builder()
                .id(1L)
                .slug(slug)
                .build();

        when(brandRepository.findBySlug(anyString())).thenReturn(Optional.of(brand));

        Brand result = brandService.getBrandBySlug(slug);

        assertEquals(brand, result);
    }

    @Test
    void getBrandBySlug_ShouldReturnBrandNotFoundException_WhenBrandDoesNotExist() {
        var slug = "brand";

        when(brandRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrandBySlug(slug));
    }
}









