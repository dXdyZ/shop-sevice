package com.example.productcatalogservice.service;

import com.example.productcatalogservice.entity.Inventory;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.exception.InventoryDuplicateException;
import com.example.productcatalogservice.exception.InventoryNotFoundException;
import com.example.productcatalogservice.repositoty.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional
    public Inventory createInventory(Product product, Integer quantity, Integer lowStockThreshold) {
        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(quantity)
                .build();
        if (lowStockThreshold != null) {
            inventory.setLowStockThreshold(lowStockThreshold);
        }
        try {
            return inventoryRepository.save(inventory);
        } catch (DataIntegrityViolationException exception) {
            throw new InventoryDuplicateException("Accounting for a product with a public id: %s already exists".formatted(product.getPublicId()));
        }
    }

    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id).orElseThrow(
                () -> new InventoryNotFoundException("Inventory by id: %s not found".formatted(id)));
    }

    public Inventory getInventoryByPublicId(UUID publicId) {
        return inventoryRepository.findByPublicId(publicId).orElseThrow(
                () -> new InventoryNotFoundException("Inventory by public id: %s not found".formatted(publicId)));
    }

    public Inventory getInventoryByProductPublicId(UUID publicId) {
        return inventoryRepository.findByProduct_PublicId(publicId).orElseThrow(
                () -> new InventoryNotFoundException("Inventory by product public id: %s not found".formatted(publicId)));
    }
}
