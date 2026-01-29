package com.fulfilment.application.monolith.assignment.adapters.restapi;

import io.smallrye.common.constraint.NotNull;

public record FulfilmentAssignmentRequest(
        @NotNull Long storeId,
        @NotNull String warehouseBusinessUnitCode,
        @NotNull Long productId
) {}
