package com.example.productcatalogservice.entity;

import com.example.productcatalogservice.exception.AttributeValueDuplicateException;
import com.example.productcatalogservice.util.mapper.SlugMapper;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@Table(name = "attributes")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Attribute implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

//    Уникальный машинный код атрибута ("color", "display-size")
    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @OneToMany(
            mappedBy = "attribute",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<AttributeValue> values = new ArrayList<>();

    @EqualsAndHashCode.Include
    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    public UUID publicId;

//    Можно ли использовать этот атрибут для фильтрации товара в каталоге
    @Builder.Default
    @Column(name = "filterable")
    private Boolean filterable = false;

//    Флаг активности атрибута
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    public AttributeValue addValue(String valueHuman) {
        String slug = SlugMapper.from(valueHuman);
        boolean exists = values.stream().anyMatch(v -> v.getSlug().equals(slug));
        if (exists) {
            throw new AttributeValueDuplicateException("Value slug '%s' already exists for attribute '%s'".formatted(slug, name));
        }

        AttributeValue attributeValue = AttributeValue.builder()
                .attribute(this)
                .value(valueHuman)
                .slug(slug)
                .build();
        values.add(attributeValue);

        return attributeValue;
    }

    public void deactivate() {
        this.isActive = false;
        values.forEach(val -> val.setIsActive(false));
    }

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
}
