package com.example.productcatalogservice.service;

import com.example.productcatalogservice.dto.create.CreateCustomAttributeDto;
import com.example.productcatalogservice.dto.create.CreateProductDto;
import com.example.productcatalogservice.entity.Brand;
import com.example.productcatalogservice.entity.Category;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.exception.BrandNotFoundException;
import com.example.productcatalogservice.exception.CategoryNotFoundException;
import com.example.productcatalogservice.exception.ProductNotFoundException;
import com.example.productcatalogservice.repositoty.jpa.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BrandService brandService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductService productService;

    private UUID brandPublicId;
    private UUID primaryCategoryPublicId;
    private UUID categoryPublicId1;
    private UUID categoryPublicId2;
    private CreateProductDto createProductDto;
    private Brand brand;
    private Category primaryCategory;
    private Category category1;
    private Category category2;
    private Product product;

    @BeforeEach
    void setUp() {
        brandPublicId = UUID.randomUUID();
        primaryCategoryPublicId = UUID.randomUUID();
        categoryPublicId1 = UUID.randomUUID();
        categoryPublicId2 = UUID.randomUUID();

        createProductDto = new CreateProductDto(
                "Test Product",
                brandPublicId,
                primaryCategoryPublicId,
                List.of(categoryPublicId1, categoryPublicId2),
                "Short description",
                "Long description",
                new BigDecimal("99.99"),
                "USD",
                1.5,
                10.0,
                5.0,
                3.0,
                List.of(new CreateCustomAttributeDto("color", "blue")),
                100,
                10
        );

        brand = Brand.builder().publicId(brandPublicId).build();
        primaryCategory = Category.builder().publicId(primaryCategoryPublicId).build();
        category1 = Category.builder().publicId(categoryPublicId1).build();
        category2 = Category.builder().publicId(categoryPublicId2).build();

        product = Product.builder()
                .name("Test Product")
                .brand(brand)
                .primaryCategory(primaryCategory)
                .build();
    }


    @Test
    void createProduct_ShouldReturnSuccessCreatedProduct_WhenBrandAndCategoryExist() {
        when(brandService.getBrandByPublicId(brandPublicId)).thenReturn(brand);
        when(categoryService.getCategoryByPublicId(primaryCategoryPublicId)).thenReturn(primaryCategory);
        when(categoryService.getCategoriesByPublicIds(anyList())).thenReturn(List.of(category1, category2));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(createProductDto);

        assertNotNull(result);
        assertEquals(product, result);

        verify(brandService).getBrandByPublicId(brandPublicId);
        verify(categoryService).getCategoryByPublicId(primaryCategoryPublicId);
        verify(categoryService).getCategoriesByPublicIds(createProductDto.categoryPublicIds());
        verify(inventoryService).createInventory(any(Product.class), eq(createProductDto.quantity()), eq(createProductDto.lowStockThreshold()));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldBrandNotFoundException_WhenBrandDoesNotExist() {
        when(brandService.getBrandByPublicId(brandPublicId)).thenThrow(new BrandNotFoundException("Brand not found"));

        assertThrows(BrandNotFoundException.class,
                () -> productService.createProduct(createProductDto));

        verifyNoInteractions(categoryService, inventoryService);
    }

    @Test
    void createProduct_ShouldCategoryNotFoundException_WhenPrimaryCategoryDoesNotExist() {
        when(brandService.getBrandByPublicId(any(UUID.class))).thenReturn(brand);
        when(categoryService.getCategoryByPublicId(any(UUID.class))).thenThrow(new CategoryNotFoundException("Category not found"));

        assertThrows(CategoryNotFoundException.class,
                () -> productService.createProduct(createProductDto));

        verify(categoryService, never()).getCategoriesByPublicIds(anyList());
        verifyNoInteractions(inventoryService);
    }

    @Test
    void createProduct_ShouldReturnSuccessCreatedProduct_WhenCategoryDoesNotExist() {
        when(brandService.getBrandByPublicId(brandPublicId)).thenReturn(brand);
        when(categoryService.getCategoryByPublicId(primaryCategoryPublicId)).thenReturn(primaryCategory);
        when(categoryService.getCategoriesByPublicIds(anyList())).thenReturn(List.of());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.createProduct(createProductDto);

        assertNotNull(result);
        assertEquals(product, result);

        verify(brandService).getBrandByPublicId(brandPublicId);
        verify(categoryService).getCategoryByPublicId(primaryCategoryPublicId);
        verify(categoryService).getCategoriesByPublicIds(createProductDto.categoryPublicIds());
        verify(inventoryService).createInventory(any(Product.class), eq(createProductDto.quantity()), eq(createProductDto.lowStockThreshold()));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_ShouldReturnProductById_WhenProductExist() {
        var id = 1L;
        product.setId(id);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        Product result = productService.getProductById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(product, result);
    }

    @Test
    void getProductById_ShouldReturnProductNotFoundException_WhenProductDoesNotExist() {
        var id = 1L;
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(id));
    }

    @Test
    void getProductByPublicId_ShouldReturnProductByPublicId_WhenProductExist() {
        var publicId = UUID.randomUUID();
        product.setPublicId(publicId);
        when(productRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(product));

        Product result = productService.getProductByPublicId(publicId);

        assertNotNull(result);
        assertEquals(publicId, result.getPublicId());
    }

    @Test
    void getProductByPublicId_ShouldReturnProductNotFoundException_WhenProductDoesNotExist() {
        var publicId = UUID.randomUUID();
        when(productRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductByPublicId(publicId));
    }

    @Test
    void getProductBuSku_ShouldReturnProductBySku_WhenProductExist() {
        var sku = "SKU-1231";
        product.setSku(sku);
        when(productRepository.findBySku(anyString())).thenReturn(Optional.of(product));

        Product result = productService.getProductBuSku(sku);

        assertNotNull(result);
        assertEquals(sku, result.getSku());
    }

    @Test
    void getProductBuSku_ShouldReturnProductNotFoundException_WhenProductDoesNotExist() {
        var sku = "SKU-1231";
        when(productRepository.findBySku(anyString())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductBuSku(sku));
    }
}






