package com.example.productcatalogservice.repositoty;

import com.example.productcatalogservice.entity.Inventory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = "product")
    Optional<Inventory> findByProduct_PublicId(UUID productPublicId);
}
