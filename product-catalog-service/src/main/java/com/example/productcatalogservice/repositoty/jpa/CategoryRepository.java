package com.example.productcatalogservice.repositoty.jpa;

import com.example.productcatalogservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByPublicId(UUID publicId);
    Optional<Category> findBySlug(String slug);
    Boolean existsByName(String name);
    List<Category> findByPublicIdIn(Collection<UUID> publicIds);
}
