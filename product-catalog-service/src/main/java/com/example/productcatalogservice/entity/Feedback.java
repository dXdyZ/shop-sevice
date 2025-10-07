package com.example.productcatalogservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@Table(name = "feedbacks")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Feedback implements Serializable {

    public enum FeedbackStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Builder.Default
    @EqualsAndHashCode.Include
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId = UUID.randomUUID();

    @Column(name = "user_public_id", nullable = false)
    private UUID userPublicId;

    @Column(name = "description")
    private String description;

    @Column(name = "estimation", nullable = false)
    private Integer estimation;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private OffsetDateTime createAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_status", nullable = false)
    private FeedbackStatus status = FeedbackStatus.PENDING;
}
