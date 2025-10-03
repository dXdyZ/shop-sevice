package com.example.productcatalogservice.dto.create;

import java.util.UUID;

public record CreateFeedback(
        String description,
        Integer estimation,
        UUID productPublicId,
) {}
