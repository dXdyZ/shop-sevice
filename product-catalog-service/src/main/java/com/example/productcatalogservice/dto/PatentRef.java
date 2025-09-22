package com.example.productcatalogservice.dto;

import java.util.UUID;

public record PatentRef(
            UUID publicId,
            String slug,
            Long id
) {}