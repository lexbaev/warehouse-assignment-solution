package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehouseValidation warehouseValidation;

  private final UserTransaction userTransaction;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidation warehouseValidation, UserTransaction userTransaction) {
      this.warehouseStore = warehouseStore;
      this.warehouseValidation = warehouseValidation;
      this.userTransaction = userTransaction;
  }

  @Override
  public void create(Warehouse warehouse) throws BusinessRuleViolationException {
    try {
      userTransaction.begin();

      warehouseValidation.verifyBusinessUnitCodeIsNew(warehouse.getBusinessUnitCode());
      Location location = warehouseValidation.validateAndGetLocation(warehouse.getLocation());
      warehouseValidation.validateCreationFeasibility(location);
      warehouseValidation.validateCapacityAndStock(location, warehouse);

      warehouseStore.create(warehouse);

      userTransaction.commit();
    } catch (Exception e) {
      try {
        userTransaction.rollback();
      } catch (Exception rollbackEx) {
        // Optionally log rollback failure
      }
      throw new IllegalStateException("Failed to create warehouse", e);
    }
  }
}
