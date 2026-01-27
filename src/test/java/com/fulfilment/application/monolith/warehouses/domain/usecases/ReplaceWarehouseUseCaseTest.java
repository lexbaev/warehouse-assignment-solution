package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import com.fulfilment.application.monolith.warehouses.mappers.DbWarehouseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReplaceWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private WarehouseValidation warehouseValidation;
    private DbWarehouseMapper mapper;
    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        warehouseValidation = mock(WarehouseValidation.class);
        mapper = mock(DbWarehouseMapper.class);
        useCase = new ReplaceWarehouseUseCase(warehouseStore, warehouseValidation, mapper);
    }

    @Test
    void replace_successful() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");
        newWarehouse.setCapacity(100);
        newWarehouse.setStock(50);

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);
        List<DbWarehouse> allWarehouses = Collections.emptyList();
        Location location = mock(Location.class);

        when(warehouseStore.getAll()).thenReturn(allWarehouses);
        when(warehouseStore.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        when(warehouseValidation.validateAndGetLocation("LOC1")).thenReturn(location);
        when(existingDbWarehouse.getStock()).thenReturn(50);
        Warehouse existingWarehouseDomain = mock(Warehouse.class);
        when(mapper.toDomain(existingDbWarehouse)).thenReturn(existingWarehouseDomain);

        useCase.replace(newWarehouse);

        verify(warehouseValidation).validateAndGetLocation("LOC1");
        verify(warehouseValidation).validateCreationFeasibility(location, allWarehouses);
        verify(warehouseValidation).validateCapacityAndStock(location, 100);
        verify(warehouseValidation).validateCapacityAccommodation(50, 100);
        verify(warehouseValidation).validateStockMatching(50, 50);
        verify(warehouseStore).remove(existingWarehouseDomain);
        verify(warehouseStore).create(newWarehouse);
    }

    @Test
    void replace_throwsException_whenWarehouseNotFound() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU2");

        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());
        when(warehouseStore.findByBusinessUnitCode("BU2")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseStore, never()).remove(any());
        verify(warehouseStore, never()).create(any());
    }

    @Test
    void replace_throwsException_whenLocationInvalid() {
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setBusinessUnitCode("BU1");
        newWarehouse.setLocation("LOC1");

        DbWarehouse existingDbWarehouse = mock(DbWarehouse.class);

        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());
        when(warehouseStore.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(existingDbWarehouse));
        doThrow(new IllegalArgumentException("Invalid location")).when(warehouseValidation)
                .validateAndGetLocation("LOC1");

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWarehouse));
        verify(warehouseStore, never()).remove(any());
        verify(warehouseStore, never()).create(any());
    }
}
