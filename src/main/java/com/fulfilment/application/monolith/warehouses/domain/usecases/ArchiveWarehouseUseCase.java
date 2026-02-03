package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.UserTransaction;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final UserTransaction userTransaction;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore, UserTransaction userTransaction) {
    this.warehouseStore = warehouseStore;
      this.userTransaction = userTransaction;
  }

  @Override
  public void archive(Warehouse warehouse) {
    try {
      userTransaction.begin();

      warehouseStore.findByBusinessUnitCode(warehouse.getBusinessUnitCode())
              .orElseThrow(() -> new BusinessRuleViolationException(
                      "Any active warehouse with business unit code " + warehouse.getBusinessUnitCode() + " is not found."));
      warehouseStore.remove(warehouse);

      userTransaction.commit();
    } catch (Exception e) {
        try {
            userTransaction.rollback();
        } catch (Exception rollbackEx) {
            // Log rollback exception if necessary
        }
        throw new IllegalStateException("Failed to archive warehouse: " + e.getMessage(), e);
    }
  }
}
