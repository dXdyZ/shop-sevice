package com.example.productcatalogservice.service;

import com.example.productcatalogservice.entity.Inventory;
import com.example.productcatalogservice.entity.Product;
import com.example.productcatalogservice.exception.InventoryDuplicateException;
import com.example.productcatalogservice.exception.InventoryNotFoundException;
import com.example.productcatalogservice.repositoty.InventoryRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;


    @Test
    void createInventory_ShouldReturnSuccessCreatedInventory_WhenLowStockThresholdIsProvidedAndDuplicateDoesNotExist() {
        var publicId = UUID.randomUUID();
        var product = Product.builder()
                .id(1L)
                .publicId(publicId)
                .build();
        var quantity = 10;
        var lowStockThreshold = 10;
        var inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .quantity(quantity)
                .lowStockThreshold(lowStockThreshold)
                .build();

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory result = inventoryService.createInventory(product, quantity, lowStockThreshold);

        assertNotNull(result);
        assertEquals(inventory.getId(), result.getId());
        assertEquals(inventory.getProduct(), result.getProduct());
        assertEquals(inventory.getQuantity(), result.getQuantity());
        assertEquals(inventory.getLowStockThreshold(), result.getLowStockThreshold());
    }

    @Test
    void createInventory_ShouldReturnSuccessCreatedInventory_WhenDuplicateDoesNotExist() {
        var publicId = UUID.randomUUID();
        var product = Product.builder()
                .id(1L)
                .publicId(publicId)
                .build();
        var quantity = 10;
        var inventory = Inventory.builder()
                .id(1L)
                .product(product)
                .quantity(quantity)
                .build();

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory result = inventoryService.createInventory(product, quantity, null);

        assertNotNull(result);
        assertEquals(inventory.getId(), result.getId());
        assertEquals(inventory.getProduct(), result.getProduct());
        assertEquals(inventory.getQuantity(), result.getQuantity());
        assertEquals(5, inventory.getLowStockThreshold());
    }

    @Test
    void createInventory_ShouldReturnInventoryDuplicateException_WhenDuplicateExist() {

        when(inventoryRepository.save(any(Inventory.class))).thenThrow(new DataIntegrityViolationException("violation restrictions"));

        assertThrows(InventoryDuplicateException.class,
                () -> inventoryService.createInventory(Product.builder()
                                .id(1L)
                        .build(), 5, null));
    }

    @Test
    void getInventoryById_ShouldReturnInventoryById_WhenInventoryExist() {
        var id = 1L;
        var inventory = Inventory.builder()
                .id(id)
                .build();

        when(inventoryRepository.findById(anyLong())).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryById(id);

        assertEquals(inventory, result);
    }

    @Test
    void getInventoryById_ShouldReturnInventoryNotFoundException_WhenInventoryDoesNotExist() {
        var id = 1L;

        when(inventoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryService.getInventoryById(id));
    }

    @Test
    void getInventoryByPublicId_ShouldReturnInventoryByPublicId_WhenInventoryExist() {
        var publicId = UUID.randomUUID();
        var inventory = Inventory.builder()
                .id(1L)
                .publicId(publicId)
                .build();

        when(inventoryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryByPublicId(publicId);

        assertEquals(inventory, result);
    }

    @Test
    void getInventoryByPublicId_ShouldReturnInventoryNotFound_WhenInventoryDoesNotExist() {
        var publicId = UUID.randomUUID();
        when(inventoryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryService.getInventoryByPublicId(publicId));
    }

    @Test
    void getInventoryByProductPublicId_ShouldReturnInventoryByProductPublicId_WhenInventoryExist() {
        var prPublicId = UUID.randomUUID();
        var inventory = Inventory.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .product(Product.builder()
                        .id(1L)
                        .publicId(prPublicId)
                        .build())
                .build();

        when(inventoryRepository.findByProduct_PublicId(any(UUID.class))).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryByProductPublicId(prPublicId);

        assertEquals(inventory, result);
    }

    @Test
    void getInventoryByProductPublicId_ShouldReturnInventoryNotFoundException_WhenInventoryDoesNotExist() {
        var prPublicId = UUID.randomUUID();
        when(inventoryRepository.findByProduct_PublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryService.getInventoryByProductPublicId(prPublicId));
    }
}








