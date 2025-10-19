package com.example.productcatalogservice.dto.event;

import java.util.List;

public record ProductCreateEvent(
        Long productId,
        Long inventoryId
) {}
