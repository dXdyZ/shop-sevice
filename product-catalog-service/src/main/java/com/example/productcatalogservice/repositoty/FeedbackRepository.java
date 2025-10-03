package com.example.productcatalogservice.repositoty;

import com.example.productcatalogservice.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Boolean existsByProduct_PublicIdAndUserPublicId(UUID productPublicId, UUID userPublicId);
    Optional<Feedback> findByPublicId(UUID publicId);
}
