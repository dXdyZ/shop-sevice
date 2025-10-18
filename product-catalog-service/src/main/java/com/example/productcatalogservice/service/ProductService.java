package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateProductDto;
import com.example.productcatalogservice.entity.*;
import com.example.productcatalogservice.exception.BrandNotFoundException;
import com.example.productcatalogservice.exception.CategoryNotFoundException;
import com.example.productcatalogservice.exception.ProductNotFoundException;
import com.example.productcatalogservice.repositoty.jpa.ProductRepository;
import com.example.productcatalogservice.util.SkuGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;

    @Transactional
    public Product createProduct(CreateProductDto createDto) throws BrandNotFoundException, CategoryNotFoundException {
        Brand brand = brandService.getBrandByPublicId(createDto.brandPublicId());
        Category primaryCategory = categoryService.getCategoryByPublicId(createDto.primaryCategoryPublicId());
        List<Category> categories = categoryService.getCategoriesByPublicIds(createDto.categoryPublicIds());

        List<CustomAttribute> customAttributes = createDto.customAttributes().stream()
                .map(custom -> {
                    return CustomAttribute.builder()
                            .name(custom.name())
                            .value(custom.value())
                            .build();
                }).toList();

        Product product = Product.builder()
                .name(createDto.name())
                .brand(brand)
                .sku(SkuGenerator.generateSku(primaryCategory.getSlug(), brand.getSlug()))
                .primaryCategory(primaryCategory)
                .description(createDto.description())
                .longDescription(createDto.longDescription())
                .basePrice(createDto.basePrice())
                .weightKg(createDto.weightKg())
                .lengthCm(createDto.lengthCm())
                .widthCm(createDto.widthCm())
                .heightCm(createDto.heightCm())
                .currency(createDto.currency())
                .build();

        categories.forEach(product::addCategory);
        customAttributes.forEach(product::addCustomAttributes);

        inventoryService.createInventory(product, createDto.quantity(), createDto.lowStockThreshold());

        return productRepository.save(product);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product by id: %s not fount".formatted(id)));
    }

    public Product getProductByPublicId(UUID publicId) {
        return productRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ProductNotFoundException("Product by public id: %s not fount".formatted(publicId)));
    }

    public Product getProductBuSku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product by sku: %s not fount".formatted(sku)));
    }

    public void updateRating(double newRating, Product product) {
        product.setRating(newRating);
        product.incRatingCount();
        productRepository.save(product);
    }
}






