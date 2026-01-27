package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehouseValidation warehouseValidation;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidation warehouseValidation) {
      this.warehouseStore = warehouseStore;
      this.warehouseValidation = warehouseValidation;
  }

  @Override
  public void create(Warehouse warehouse) {
    // My assumption that number of elements is not large (<1000), so fetching all is acceptable
    List<DbWarehouse> warehouseStoreAll = warehouseStore.getAll();

    warehouseValidation.verifyBusinessUnitCodeIsNew(warehouse.getBusinessUnitCode(), warehouseStoreAll);
    Location location = warehouseValidation.validateAndGetLocation(warehouse.getLocation());
    warehouseValidation.validateCreationFeasibility(location, warehouseStoreAll);
    warehouseValidation.validateCapacityAndStock(location, warehouse.getCapacity());

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
