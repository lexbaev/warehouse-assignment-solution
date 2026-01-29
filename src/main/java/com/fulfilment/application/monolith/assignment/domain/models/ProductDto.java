package com.fulfilment.application.monolith.assignment.domain.models;

import java.math.BigDecimal;

public record ProductDto(
        // A product should have a unique identifier field that is different from id field. For simplicity, we will use Long id.
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock
) {}
