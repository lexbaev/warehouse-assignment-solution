package com.fulfilment.application.monolith.warehouses.mappers;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.ZoneId;

@ApplicationScoped
public class DbWarehouseMapper {

    @Nullable
    public DbWarehouse toEntity(Warehouse warehouse) {
        if (warehouse == null) return null;
        DbWarehouse db = new DbWarehouse();
        db.setBusinessUnitCode(warehouse.getBusinessUnitCode());
        db.setLocation(warehouse.getLocation());
        db.setCapacity(warehouse.getCapacity());
        db.setStock(warehouse.getStock());
        db.setCreatedAt(warehouse.getCreationAt() != null
                ? warehouse.getCreationAt().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                : null);
        db.setArchivedAt(warehouse.getArchivedAt() != null
                ? warehouse.getArchivedAt().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                : null);
        return db;
    }

    public void updateEntityFromDto(Warehouse warehouse, DbWarehouse entity) {
        if (warehouse == null || entity == null) return;
        entity.setBusinessUnitCode(warehouse.getBusinessUnitCode());
        entity.setLocation(warehouse.getLocation());
        entity.setCapacity(warehouse.getCapacity());
        entity.setStock(warehouse.getStock());
        entity.setArchivedAt(warehouse.getArchivedAt() != null
                ? warehouse.getArchivedAt().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                : null);
        // Do not update businessUnitCode or createdAt to preserve identity and creation timestamp
    }

    @Nullable
    public Warehouse toDomain(DbWarehouse dbWarehouse) {
        if (dbWarehouse == null) return null;

        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode(dbWarehouse.getBusinessUnitCode());
        warehouse.setLocation(dbWarehouse.getLocation());
        warehouse.setCapacity(dbWarehouse.getCapacity());
        warehouse.setStock(dbWarehouse.getStock());

        warehouse.setCreationAt(dbWarehouse.getCreatedAt() != null
                ? dbWarehouse.getCreatedAt().atZone(ZoneId.systemDefault())
                : null);

        warehouse.setArchivedAt(dbWarehouse.getArchivedAt() != null
                ? dbWarehouse.getArchivedAt().atZone(ZoneId.systemDefault())
                : null);

        return warehouse;
    }
}