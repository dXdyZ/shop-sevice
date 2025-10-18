package com.example.productcatalogservice.repositoty.jpa;

import com.example.productcatalogservice.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    Boolean existsByName(String name);
    Optional<Attribute> findByPublicId(UUID publicId);
    Optional<Attribute> findByName(String name);
    Optional<Attribute> findBySlug(String slug);
}
