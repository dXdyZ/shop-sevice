package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.CreateBrandDto;
import com.example.productcatalogservice.entity.Brand;
import com.example.productcatalogservice.exception.BrandNotFoundException;
import com.example.productcatalogservice.repositoty.BrandRepository;
import com.example.productcatalogservice.util.BrandMapper;
import com.example.productcatalogservice.util.SlugMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public Brand createBrand(CreateBrandDto createDto) {
        Brand brand =  brandRepository.save(BrandMapper.fromDto(createDto));
        brand.setSlug(SlugMapper.fromName(createDto.name()));
        return brandRepository.save(brand);
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
