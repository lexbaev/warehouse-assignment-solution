package com.fulfilment.application.monolith.assignment.domain.models;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public record FulfilmentAssignmentDto(
        Long id,
        StoreDto store,
        ProductDto product,
        Warehouse warehouse
) {}
