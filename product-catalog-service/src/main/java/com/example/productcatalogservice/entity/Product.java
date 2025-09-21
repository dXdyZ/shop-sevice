package com.example.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Уникальный артикуль товара для склада
    @Column(name = "stock_keeping_unit", unique = true, nullable = false, length = 100)
    private String stockKeepingUnit;

    @Column(name = "name", nullable = false)
    private String name;

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
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}






