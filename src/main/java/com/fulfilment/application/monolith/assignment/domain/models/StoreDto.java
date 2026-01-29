package com.fulfilment.application.monolith.assignment.domain.models;

public record StoreDto(
        // A store should have a unique identifier field that is different from id field. For simplicity, we will use Long id.
        Long id,
        String name,
        int quantityProductsInStock
) {}
