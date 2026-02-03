package com.fulfilment.application.monolith.warehouses.domain.validations;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Objects;

@ApplicationScoped
public class WarehouseValidation {

    private final LocationResolver locationResolver;

    private final WarehouseRepository warehouseRepository;

    public WarehouseValidation(LocationResolver locationResolver, WarehouseRepository warehouseRepository) {
        this.locationResolver = locationResolver;
        this.warehouseRepository = warehouseRepository;
    }

    public void verifyBusinessUnitCodeIsNew(String businessUnitCode) throws BusinessRuleViolationException {
        if (warehouseRepository.findByBusinessUnitCode(businessUnitCode).isPresent()) {
            throw new BusinessRuleViolationException("Warehouse with business unit code " + businessUnitCode + " already exists.");
        }
    }

    public Location validateAndGetLocation(String identifier) throws BusinessRuleViolationException {
        return locationResolver.resolveByIdentifier(identifier)
                .orElseThrow(() -> new BusinessRuleViolationException("Location not found for identifier: " + identifier));
    }

    public void validateCreationFeasibility(Location location) throws BusinessRuleViolationException {
        if (!warehouseRepository.isCreationOrReplacementFeasible(location.identification(), location.maxNumberOfWarehouses())) {
            throw new BusinessRuleViolationException("The warehouse cannot be created. Location " + location.identification() + " has reached its maximum number of warehouses.");
        }
    }

    public void validateReplacementFeasibility(Location location) throws BusinessRuleViolationException {
        if (!warehouseRepository.isCreationOrReplacementFeasible(location.identification(), location.maxNumberOfWarehouses() + 1)) {
            throw new BusinessRuleViolationException("The warehouse cannot be created. Location " + location.identification() + " has reached its maximum number of warehouses.");
        }
    }

    public void validateCapacityAndStock(Location location, Warehouse warehouse) throws BusinessRuleViolationException {
        if (warehouse.getCapacity() > location.maxCapacity()) {
            throw new BusinessRuleViolationException("The warehouse capacity exceeds the maximum allowed capacity for location " + location.identification() + ".");
        }

        if (warehouse.getStock() > warehouse.getCapacity()) {
            throw new BusinessRuleViolationException("The warehouse capacity cannot be less than its stock.");
        }
    }

    public void validateCapacityAccommodation(Integer existingWarehouseStock, Integer newWarehouseCapacity) throws BusinessRuleViolationException {
        if (newWarehouseCapacity < existingWarehouseStock) {
            throw new BusinessRuleViolationException("The new warehouse capacity cannot accommodate the existing stock.");
        }
    }

    public void validateStockMatching(Integer existingWarehouseStock, Integer newWarehouseStock) throws BusinessRuleViolationException {
        if (!Objects.equals(existingWarehouseStock, newWarehouseStock)) {
            throw new BusinessRuleViolationException("The stock of the new warehouse does not match the stock of the previous warehouse.");
        }
    }
}
