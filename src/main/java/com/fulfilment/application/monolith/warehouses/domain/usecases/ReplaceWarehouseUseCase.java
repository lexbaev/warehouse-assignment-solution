package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import com.fulfilment.application.monolith.warehouses.mappers.DbWarehouseMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehouseValidation warehouseValidation;

  private final UserTransaction userTransaction;

  private final DbWarehouseMapper mapper;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidation warehouseValidation, UserTransaction userTransaction, DbWarehouseMapper mapper) {
    this.warehouseStore = warehouseStore;
      this.warehouseValidation = warehouseValidation;
      this.userTransaction = userTransaction;
      this.mapper = mapper;
  }

  @Override
  public void replace(Warehouse newWarehouse) throws BusinessRuleViolationException {
    try {
      userTransaction.begin();

      DbWarehouse existingWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.getBusinessUnitCode())
              .orElseThrow(() -> new BusinessRuleViolationException(
                      "Any active warehouse with business unit code " + newWarehouse.getBusinessUnitCode() + " is not found."));

      Location location = warehouseValidation.validateAndGetLocation(newWarehouse.getLocation());
      warehouseValidation.validateReplacementFeasibility(location);
      warehouseValidation.validateCapacityAndStock(location, newWarehouse);
      warehouseValidation.validateCapacityAccommodation(existingWarehouse.getStock(), newWarehouse.getCapacity());
      warehouseValidation.validateStockMatching(existingWarehouse.getStock(), newWarehouse.getStock());

      warehouseStore.remove(mapper.toDomain(existingWarehouse));
      warehouseStore.create(newWarehouse);

      userTransaction.commit();
    } catch (Exception e) {
      try {
        userTransaction.rollback();
      } catch (Exception rollbackEx) {
        // Optionally log rollback failure
      }
      throw new IllegalStateException("Failed to replace warehouse", e);
    }
  }
}
