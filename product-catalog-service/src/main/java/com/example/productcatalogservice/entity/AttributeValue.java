package com.example.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@Table(name = "attribute_values")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "attribute_id",
            nullable = false
    )
    private Attribute attribute;

    @EqualsAndHashCode.Include
    @Builder.Default
    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    public UUID publicId = UUID.randomUUID();

//    Человеко-читаемое значение ("Red")
    @Column(name = "value", nullable = false)
    private String value;

//    Машинное представление значения для URL и фильтров ("red")
    @Column(name = "slug", nullable = false)
    private String slug;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}
