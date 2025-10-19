package com.example.productcatalogservice.service.elastic;

import com.example.productcatalogservice.elastic_document.ProductDoc;
import com.example.productcatalogservice.entity.Inventory;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.service.InventoryService;
import com.example.productcatalogservice.service.ProductService;
import com.example.productcatalogservice.util.mapper.ProductDocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductDataProvider {
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final ProductDocumentMapper productDocumentMapper;

    public ProductDoc getProductData(Long productId, Long inventoryId) {
        Product product = productService.getProductById(productId);
        Optional<Inventory> inventory = Optional.of(inventoryService.getInventoryById(inventoryId));

        return productDocumentMapper.toProduct(product, inventory);
    }
}
