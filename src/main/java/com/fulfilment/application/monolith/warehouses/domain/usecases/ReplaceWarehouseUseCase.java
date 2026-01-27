package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import com.fulfilment.application.monolith.warehouses.mappers.DbWarehouseMapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehouseValidation warehouseValidation;

  private final DbWarehouseMapper mapper;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidation warehouseValidation, DbWarehouseMapper mapper) {
    this.warehouseStore = warehouseStore;
      this.warehouseValidation = warehouseValidation;
      this.mapper = mapper;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // My assumption that number of elements is not large (<1000), so fetching all is acceptable
    List<DbWarehouse> warehouseStoreAll = warehouseStore.getAll();

    DbWarehouse existingWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode())
            .orElseThrow(() -> new IllegalArgumentException(
                    "Any active warehouse with business unit code " + newWarehouse.getBusinessUnitCode() + " is not found."));

    Location location = warehouseValidation.validateAndGetLocation(newWarehouse.getLocation());
    warehouseValidation.validateCreationFeasibility(location, warehouseStoreAll);
    warehouseValidation.validateCapacityAndStock(location, newWarehouse.getCapacity());
    warehouseValidation.validateCapacityAccommodation(existingWarehouse.getStock(), newWarehouse.getCapacity());
    warehouseValidation.validateStockMatching(existingWarehouse.getStock(), newWarehouse.getStock());

    warehouseStore.remove(mapper.toDomain(existingWarehouse));
    warehouseStore.create(newWarehouse);
  }
}
