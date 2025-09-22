package com.example.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@Table(name = "attributes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

//    Уникальный машинный код атрибута ("color", "display-size")
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @EqualsAndHashCode.Include
    @Builder.Default
    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    public UUID publicId = UUID.randomUUID();

//    Можно ли использовать этот атрибут для фильтрации товара в каталоге
    @Builder.Default
    @Column(name = "filterable")
    private Boolean filterable = false;

//    Флаг активности атрибута
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
