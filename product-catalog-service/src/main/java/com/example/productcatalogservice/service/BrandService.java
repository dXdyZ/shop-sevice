package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateBrandDto;
import com.example.productcatalogservice.entity.Brand;
import com.example.productcatalogservice.exception.BrandDuplicateException;
import com.example.productcatalogservice.exception.BrandNotFoundException;
import com.example.productcatalogservice.repositoty.BrandRepository;
import com.example.productcatalogservice.util.BrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public Brand createBrand(CreateBrandDto createDto) {
        try {
            return brandRepository.save(BrandMapper.fromCreateDto(createDto));
        } catch (DataIntegrityViolationException exception) {
            throw new BrandDuplicateException("Brand by name: %s already exists".formatted(createDto.name()));
        }
    }

    public Brand getBrandById(Long id) {
        return brandRepository.findById(id).orElseThrow(
                () -> new BrandNotFoundException("Brand not found"));
    }

    public Brand getBrandByPublicId(UUID publicId) {
        return brandRepository.findByPublicId(publicId).orElseThrow(
                () -> new BrandNotFoundException("Brand not found"));
    }

    public Brand getBrandBySlug(String slug) {
        return brandRepository.findBySlug(slug).orElseThrow(
                () -> new BrandNotFoundException("Brand not found"));
    }
}
