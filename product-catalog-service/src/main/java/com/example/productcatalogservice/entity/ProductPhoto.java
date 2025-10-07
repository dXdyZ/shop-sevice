package com.example.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@Table(name = "product_photos")
@NoArgsConstructor
@AllArgsConstructor
public class ProductPhoto implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
