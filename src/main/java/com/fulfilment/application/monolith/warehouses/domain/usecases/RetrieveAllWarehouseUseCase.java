package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.ports.RetrieveAllWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.WarehouseMapper;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RetrieveAllWarehouseUseCase implements RetrieveAllWarehouseOperation {

    private final WarehouseStore warehouseStore;

    private final WarehouseMapper mapper;

    public RetrieveAllWarehouseUseCase(WarehouseStore warehouseStore, WarehouseMapper mapper) {
        this.warehouseStore = warehouseStore;
        this.mapper = mapper;
    }

    @Override
    public List<Warehouse> getAll() {
        return warehouseStore.getAll().stream()
                .map(mapper::toApiBean).toList();
    }
}
