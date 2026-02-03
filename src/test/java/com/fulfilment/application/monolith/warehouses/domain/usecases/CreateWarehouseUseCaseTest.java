package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateWarehouseUseCaseTest {
    private WarehouseRepository warehouseRepository;
    private LocationResolver locationResolver;
    private CreateWarehouseUseCase useCase;
    private UserTransaction userTransaction;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        locationResolver = mock(LocationResolver.class);
        userTransaction = mock(UserTransaction.class);
        WarehouseValidation warehouseValidation = new WarehouseValidation(locationResolver, warehouseRepository);
        useCase = new CreateWarehouseUseCase(warehouseRepository, warehouseValidation, userTransaction);
    }

    @Test
    void create_successful() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation("LOC1");
        warehouse.setCapacity(100);
        warehouse.setStock(50);

        Location location = new Location("LOC1", 5, 200);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.empty());
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 5)).thenReturn(true);

        assertDoesNotThrow(() -> useCase.create(warehouse));
        verify(warehouseRepository).create(warehouse);
        verify(userTransaction).begin();
        verify(userTransaction).commit();
    }

    @Test
    void create_throwsException_whenBusinessUnitCodeNotNew() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation("LOC1");
        warehouse.setCapacity(100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(mock(DbWarehouse.class)));

        assertThrows(IllegalStateException.class, () -> useCase.create(warehouse));
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void create_throwsException_whenLocationInvalid() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation("LOC1");
        warehouse.setCapacity(100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.empty());
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.create(warehouse));
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void create_throwsException_whenCreationNotFeasible() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation("LOC1");
        warehouse.setCapacity(100);

        Location location = new Location("LOC1", 1, 500);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.empty());
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 1)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> useCase.create(warehouse));
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void create_throwsException_whenCapacityExceedsLocation() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation("LOC1");
        warehouse.setCapacity(300); // exceeds location max
        warehouse.setStock(50);

        Location location = new Location("LOC1", 5, 200);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.empty());
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 5)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> useCase.create(warehouse));
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void create_throwsException_whenCapacityLessThanStock() throws Exception {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation("LOC1");
        warehouse.setCapacity(40); // less than stock
        warehouse.setStock(50);

        Location location = new Location("LOC1", 5, 200);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.empty());
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible("LOC1", 5)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> useCase.create(warehouse));
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }
}