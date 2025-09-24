package com.example.productcatalogservice.service;

import com.example.productcatalogservice.entity.Inventory;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.repositoty.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public Inventory createInventory(Product product, Integer quantity, Integer lowStockThreshold) {
        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(quantity)
                .build();
        if (lowStockThreshold != null) {
            inventory.setLowStockThreshold(lowStockThreshold);
        }
        return inventoryRepository.save(inventory);
    }
}
