package com.fulfilment.application.monolith.warehouses.domain.validations;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WarehouseValidation {

    private final LocationResolver locationResolver;

    public WarehouseValidation(LocationResolver locationResolver) {
        this.locationResolver = locationResolver;
    }

    public void verifyBusinessUnitCodeIsNew(String businessUnitCode, List<DbWarehouse> warehouseStoreAll) {
        if (warehouseStoreAll.stream()
                .anyMatch(dbWarehouse -> dbWarehouse.getBusinessUnitCode().equals(businessUnitCode))) {;
            throw new IllegalArgumentException("Warehouse with business unit code " + businessUnitCode + " already exists.");
        }
    }

    public Location validateAndGetLocation(String identifier) {
        return locationResolver.resolveByIdentifier(identifier);
    }

    public void validateCreationFeasibility(Location location, List<DbWarehouse> warehouseStoreAll) {
        long numberOfExistingWarehousesOnLocation = warehouseStoreAll.stream()
                .filter(dbWarehouse -> dbWarehouse.getLocation().equals(location.identification()))
                .count();
        if (numberOfExistingWarehousesOnLocation >= location.maxNumberOfWarehouses()) {
            throw new IllegalArgumentException("The warehouse cannot be created. Location " + location.identification() + " has reached its maximum number of warehouses.");
        }
    }

    public void validateCapacityAndStock(Location location, Integer warehouseCapacity) {
        if (warehouseCapacity > location.maxCapacity()) {
            throw new IllegalArgumentException("The warehouse capacity exceeds the maximum allowed capacity for location " + location.identification() + ".");
        }
    }

    public void validateCapacityAccommodation(Integer existingWarehouseStock, Integer newWarehouseCapacity) {
        if (newWarehouseCapacity < existingWarehouseStock) {
            throw new IllegalArgumentException("The new warehouse capacity cannot accommodate the existing stock.");
        }
    }

    public void validateStockMatching(Integer existingWarehouseStock, Integer newWarehouseStock) {
        if (!Objects.equals(existingWarehouseStock, newWarehouseStock)) {
            throw new IllegalArgumentException("The stock of the new warehouse does not match the stock of the previous warehouse.");
        }
    }
}
