package com.example.productcatalogservice.repositoty.jpa;

import com.example.productcatalogservice.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {
    List<AttributeValue> findAllByPublicIdIn(Collection<UUID> publicIds);
    List<AttributeValue> findAllByIdIn(Collection<Long> ids);
}
