package com.fulfilment.application.monolith.warehouses.domain.validations;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WarehouseValidationTest {

    private LocationResolver locationResolver;
    private WarehouseRepository warehouseRepository;
    private WarehouseValidation validation;

    @BeforeEach
    void setUp() {
        locationResolver = mock(LocationResolver.class);
        warehouseRepository = mock(WarehouseRepository.class);
        validation = new WarehouseValidation(locationResolver, warehouseRepository);
    }

    @Test
    void verifyBusinessUnitCodeIsNew_success() {
        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> validation.verifyBusinessUnitCodeIsNew("BU1"));
    }

    @Test
    void verifyBusinessUnitCodeIsNew_failure() {
        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(mock(DbWarehouse.class)));
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.verifyBusinessUnitCodeIsNew("BU1"));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void validateAndGetLocation_success() throws BusinessRuleViolationException {
        Location location = new Location("LOC1", 2, 100);
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        assertEquals(location, validation.validateAndGetLocation("LOC1"));
    }

    @Test
    void validateAndGetLocation_failure() {
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.empty());
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateAndGetLocation("LOC1"));
        assertTrue(ex.getMessage().contains("Location not found"));
    }

    @Test
    void validateCreationFeasibility_success() {
        Location location = new Location("LOC1", 2, 100);
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 2)).thenReturn(true);
        assertDoesNotThrow(() -> validation.validateCreationFeasibility(location));
    }

    @Test
    void validateCreationFeasibility_failure() {
        Location location = new Location("LOC1", 2, 100);
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 2)).thenReturn(false);
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateCreationFeasibility(location));
        assertTrue(ex.getMessage().contains("has reached its maximum number of warehouses"));
    }

    @Test
    void validateReplacementFeasibility_success() {
        Location location = new Location("LOC1", 2, 100);
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 3)).thenReturn(true);
        assertDoesNotThrow(() -> validation.validateReplacementFeasibility(location));
    }

    @Test
    void validateReplacementFeasibility_failure() {
        Location location = new Location("LOC1", 2, 100);
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 3)).thenReturn(false);
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateReplacementFeasibility(location));
        assertTrue(ex.getMessage().contains("has reached its maximum number of warehouses"));
    }

    @Test
    void validateCapacityAndStock_success() {
        Location location = new Location("LOC1", 2, 100);
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(80);
        warehouse.setStock(50);
        assertDoesNotThrow(() -> validation.validateCapacityAndStock(location, warehouse));
    }

    @Test
    void validateCapacityAndStock_failure_capacityExceeds() {
        Location location = new Location("LOC1", 2, 100);
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(120);
        warehouse.setStock(50);
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateCapacityAndStock(location, warehouse));
        assertTrue(ex.getMessage().contains("capacity exceeds"));
    }

    @Test
    void validateCapacityAndStock_failure_stockExceedsCapacity() {
        Location location = new Location("LOC1", 2, 100);
        Warehouse warehouse = new Warehouse();
        warehouse.setCapacity(40);
        warehouse.setStock(50);
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateCapacityAndStock(location, warehouse));
        assertTrue(ex.getMessage().contains("capacity cannot be less than its stock"));
    }

    @Test
    void validateCapacityAccommodation_success() {
        assertDoesNotThrow(() -> validation.validateCapacityAccommodation(50, 60));
    }

    @Test
    void validateCapacityAccommodation_failure() {
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateCapacityAccommodation(50, 40));
        assertTrue(ex.getMessage().contains("cannot accommodate the existing stock"));
    }

    @Test
    void validateStockMatching_success() {
        assertDoesNotThrow(() -> validation.validateStockMatching(50, 50));
    }

    @Test
    void validateStockMatching_failure() {
        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class,
                () -> validation.validateStockMatching(50, 40));
        assertTrue(ex.getMessage().contains("does not match the stock"));
    }
}