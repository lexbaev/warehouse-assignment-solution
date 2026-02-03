package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import com.fulfilment.application.monolith.warehouses.mappers.DbWarehouseMapper;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReplaceWarehouseUseCaseTest {

    private WarehouseRepository warehouseRepository;
    private LocationResolver locationResolver;
    private DbWarehouseMapper mapper;
    private UserTransaction userTransaction;
    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseRepository = mock(WarehouseRepository.class);
        locationResolver = mock(LocationResolver.class);
        WarehouseValidation warehouseValidation = new WarehouseValidation(locationResolver, warehouseRepository);
        mapper = mock(DbWarehouseMapper.class);
        userTransaction = mock(UserTransaction.class);
        useCase = new ReplaceWarehouseUseCase(warehouseRepository, warehouseValidation, userTransaction, mapper);
    }

    @Test
    void replace_successful() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(100);
        newWarehouse.setStock(50);

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        Location location = new Location("LOC1", 1, 100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible(eq("LOC1"), eq(2))).thenReturn(true);
        when(existingDbWarehouse.getStock()).thenReturn(50);
        Warehouse existingWarehouseDomain = mock(Warehouse.class);
        when(mapper.toDomain(existingDbWarehouse)).thenReturn(existingWarehouseDomain);

        useCase.replace(newWarehouse);

        verify(warehouseRepository).findByBusinessUnitCode("BU1");
        verify(locationResolver).resolveByIdentifier("LOC1");
        verify(warehouseRepository).isCreationOrReplacementFeasible(eq("LOC1"), eq(2));
        verify(mapper).toDomain(existingDbWarehouse);
        verify(warehouseRepository).remove(existingWarehouseDomain);
        verify(warehouseRepository).create(newWarehouse);
        verify(userTransaction).begin();
        verify(userTransaction).commit();
    }

    @Test
    void replace_throwsException_whenWarehouseNotFound() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU2");

        when(warehouseRepository.findByBusinessUnitCode("BU2")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void replace_throwsException_whenLocationInvalid() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void replace_throwsException_whenReplacementNotFeasible() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(100);
        newWarehouse.setStock(50);

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        Location location = new Location("LOC1", 1, 100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible(eq("LOC1"), eq(2))).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void replace_throwsException_whenCapacityTooLow() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(40); // less than stock
        newWarehouse.setStock(50);

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        Location location = new Location("LOC1", 1, 100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible(eq("LOC1"), eq(2))).thenReturn(true);
        when(existingDbWarehouse.getStock()).thenReturn(50);

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void replace_throwsException_whenCapacityExceedsLocation() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(200); // exceeds location max
        newWarehouse.setStock(50);

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        Location location = new Location("LOC1", 1, 100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible(eq("LOC1"), eq(2))).thenReturn(true);
        when(existingDbWarehouse.getStock()).thenReturn(50);

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void replace_throwsException_whenCapacityCannotAccommodateStock() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(40); // less than existing stock
        newWarehouse.setStock(40);

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        Location location = new Location("LOC1", 1, 100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible(eq("LOC1"), eq(2))).thenReturn(true);
        when(existingDbWarehouse.getStock()).thenReturn(50);

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }

    @Test
    void replace_throwsException_whenStockDoesNotMatch() throws Exception {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(100);
        newWarehouse.setStock(40); // does not match existing

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        Location location = new Location("LOC1", 1, 100);

        when(warehouseRepository.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(Optional.of(location));
        when(warehouseRepository.isCreationOrReplacementFeasible(eq("LOC1"), eq(2))).thenReturn(true);
        when(existingDbWarehouse.getStock()).thenReturn(50);

        assertThrows(IllegalStateException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseRepository, never()).remove(any());
        verify(warehouseRepository, never()).create(any());
        verify(userTransaction).begin();
        verify(userTransaction).rollback();
    }
}