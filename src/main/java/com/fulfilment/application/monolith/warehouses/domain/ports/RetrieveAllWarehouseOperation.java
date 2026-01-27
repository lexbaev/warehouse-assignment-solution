package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.warehouse.api.beans.Warehouse;

import java.util.List;

public interface RetrieveAllWarehouseOperation {
    List<Warehouse> getAll();
}
