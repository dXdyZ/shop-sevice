package com.example.productcatalogservice.repositoty;

import com.example.productcatalogservice.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByPublicId(UUID publicId);
    Optional<Brand> findBySlug(String slug);
    Boolean existsByName(String name);
}
