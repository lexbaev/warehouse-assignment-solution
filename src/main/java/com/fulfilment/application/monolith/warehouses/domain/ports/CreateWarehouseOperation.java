package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

public interface CreateWarehouseOperation {
  void create(Warehouse warehouse) throws BusinessRuleViolationException;
}
