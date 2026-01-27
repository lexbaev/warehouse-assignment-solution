package com.fulfilment.application.monolith.warehouses.mappers;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.warehouse.api.beans.Warehouse;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseMapper {

    @Nullable
    public Warehouse toApiBean(DbWarehouse dbWarehouse) {
        if (dbWarehouse == null) return null;

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode(dbWarehouse.getBusinessUnitCode());
        warehouse.setLocation(dbWarehouse.getLocation());
        warehouse.setCapacity(dbWarehouse.getCapacity());
        warehouse.setStock(dbWarehouse.getStock());

        return warehouse;
    }

    @Nullable
    public DbWarehouse toEntity(Warehouse warehouse) {
        if (warehouse == null) return null;

        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.setLocation(warehouse.getLocation());
        dbWarehouse.setCapacity(warehouse.getCapacity());
        dbWarehouse.setStock(warehouse.getStock());

        return dbWarehouse;
    }

    // Converts API bean to domain model
    @Nullable
    public com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(Warehouse apiWarehouse) {
        if (apiWarehouse == null) return null;
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        domainWarehouse.setBusinessUnitCode(apiWarehouse.getBusinessUnitCode());
        domainWarehouse.setLocation(apiWarehouse.getLocation());
        domainWarehouse.setCapacity(apiWarehouse.getCapacity());
        domainWarehouse.setStock(apiWarehouse.getStock());
        // creationAt and archivedAt are not present in API bean
        return domainWarehouse;
    }

    // Converts domain model to API bean
    @Nullable
    public Warehouse toApiBean(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse) {
        if (domainWarehouse == null) return null;
        com.warehouse.api.beans.Warehouse apiWarehouse = new com.warehouse.api.beans.Warehouse();
        apiWarehouse.setBusinessUnitCode(domainWarehouse.getBusinessUnitCode());
        apiWarehouse.setLocation(domainWarehouse.getLocation());
        apiWarehouse.setCapacity(domainWarehouse.getCapacity());
        apiWarehouse.setStock(domainWarehouse.getStock());
        // id is not present in domain model
        return apiWarehouse;
    }
}