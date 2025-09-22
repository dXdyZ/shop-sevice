package com.example.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "products")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Уникальный артикуль товара для склада
    @Column(name = "stock_keeping_unit", unique = true, nullable = false, length = 100)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @EqualsAndHashCode.Include
    @Builder.Default
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "primary_category_id")
    private Category primaryCategory;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new LinkedHashSet<>();

    //Короткое описание товара
    @Column(name = "description")
    private String description;

    //Полное описание товара
    @Column(name = "long_description")
    private String longDescription;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    //Валюта в которой указана цена
    @Builder.Default
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "RUB";

    //Флаг, разрешено ли показывать товар в каталоге. По умолчанию скрыт до проверки админом
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    //Флаг, разрешено ли добавить товар в корзину и купить. По умолчанию нельзя до проверки админом
    @Builder.Default
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    //Вес товара
    @Column(name = "wight_kg")
    private Double wightKg;

    //Длинна упаковки
    @Column(name = "length_cm")
    private Double lengthCm;

    //Ширина упаковки
    @Column(name = "width_cm")
    private Double widthCm;

    //Высота упаковки
    @Column(name = "height_cm")
    private Double heightCm;

    @Builder.Default
    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL
    )
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public void addAttributeValue(AttributeValue value) {
        if (value == null) return;
        boolean exists = attributeValues.stream()
                .anyMatch(pav -> pav.getAttributeValue().getId().equals(value.getId()));
        if (!exists) {
            ProductAttributeValue link = new ProductAttributeValue(this, value);
            attributeValues.add(link);
        }
    }

    public void removeAttributeValue(AttributeValue value) {
        if (value == null) return;
        attributeValues.removeIf(pav -> {
            boolean match = pav.getAttributeValue().getId().equals(value.getId());
            if (match) {
                pav.setProduct(null);
                pav.setAttributeValue(null);
            }
            return match;
        });
    }
}






