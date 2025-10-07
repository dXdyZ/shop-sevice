package com.example.productcatalogservice.entity;

import com.example.productcatalogservice.util.SkuGenerator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
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
public class Product implements Serializable {

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
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne
    @JoinColumn(name = "primary_category_id")
    private Category primaryCategory;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
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
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    //Флаг, разрешено ли добавить товар в корзину и купить. По умолчанию нельзя до проверки админом
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    //Вес товара
    @Column(name = "wight_kg")
    private Double weightKg;

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
    private Long ratingCount = 0L;

    @Column(name = "rating")
    private Double rating;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProductAttributeValue> attributeValues = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "product_id")
    @Builder.Default
    private List<CustomAttribute> customAttributes = new ArrayList<>();

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.sku == null) {
            this.sku = SkuGenerator.generateSku(primaryCategory.getSlug(), brand.getSlug());
        }
        if (publicId == null) {
            this.publicId = UUID.randomUUID();
        }
        if (isActive == null) {
            this.isActive = false;
        }
        if (isAvailable == null) {
            this.isAvailable = false;
        }
    }

    public void addCategory(Category value) {
        if (value == null) return;
        boolean exists = categories.stream()
                .anyMatch(pav -> pav.getPublicId().equals(value.getPublicId()));
        if (!exists) {
            categories.add(value);
        }
    }

    public void addAttributeValue(AttributeValue value) {
        if (value == null) return;
        boolean exists = attributeValues.stream()
                .anyMatch(pav -> pav.getAttributeValue().getId().equals(value.getId()));
        if (!exists) {
            ProductAttributeValue link = new ProductAttributeValue(this, value);
            attributeValues.add(link);
        }
    }

    public void addCustomAttributes(CustomAttribute value) {
        if (value == null) return;
        boolean exists = customAttributes.stream()
                .anyMatch(pav -> pav.getName().equalsIgnoreCase(value.getName()));
        if (!exists) {
            customAttributes.add(value);
        }
    }

    public void removeCustomAttribute(CustomAttribute value) {
        if (value == null) return;
        customAttributes.removeIf(pav -> pav.getId().equals(value.getId()));
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

    public void incRatingCount() {
        this.ratingCount++;
    }
}






