package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;
import java.util.Optional;

public interface WarehouseStore {

  // This method should return List<DbWarehouse> instead of List<Warehouse> type, because a repository returns entity type anyway.
  // And later on we should map the entity to domain model, or API model for response
  // Also, this method can lead to memory leaking problem if there are too many records in the table.
  // My assumption is that expected maximum of elements is around 1000.
  // Otherwise, I would use pagination here and for validations I would use methods with criteria parameters.
  List<DbWarehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  void remove(Warehouse warehouse);

  // This method should return Optional<DbWarehouse> instead of Warehouse type, because a repository returns entity type anyway.
  // And later on we should map the entity to domain model, or API model for response
  Optional<DbWarehouse> findByBusinessUnitCode(String buCode);

  Optional<DbWarehouse> findById(String id);
}
