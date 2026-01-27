package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveUnitByIdWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.WarehouseMapper;
import com.warehouse.api.beans.Warehouse;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RetrieveUnitByIdWarehouseUseCase implements RetrieveUnitByIdWarehouseOperation {

    private final WarehouseStore warehouseStore;

    private final WarehouseMapper mapper;

    public RetrieveUnitByIdWarehouseUseCase(WarehouseStore warehouseStore, WarehouseMapper mapper) {
        this.warehouseStore = warehouseStore;
        this.mapper = mapper;
    }

    @Override
    @Nullable
    public Warehouse getById(String id) {
        return warehouseStore.findById(id)
                .map(mapper::toApiBean)
                .orElse(null);
    }
}
