package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.warehouse.api.beans.Warehouse;

public interface RetrieveUnitByIdWarehouseOperation {
    Warehouse getById(String id);
}
