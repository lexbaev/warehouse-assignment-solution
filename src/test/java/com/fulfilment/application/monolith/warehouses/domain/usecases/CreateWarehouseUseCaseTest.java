package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validations.WarehouseValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CreateWarehouseUseCaseTest {
    public static final String ZWOLLE_001 = "ZWOLLE-001";
    private WarehouseStore warehouseStore;
    private WarehouseValidation warehouseValidation;
    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        warehouseValidation = mock(WarehouseValidation.class);
        useCase = new CreateWarehouseUseCase(warehouseStore, warehouseValidation);
    }

    @Test
    void create_successful() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        Location location = new Location(ZWOLLE_001, 5, 500);
        warehouse.setLocation(ZWOLLE_001);
        warehouse.setCapacity(100);

        List<DbWarehouse> allWarehouses = Collections.emptyList();
        when(warehouseStore.getAll()).thenReturn(allWarehouses);
        when(warehouseValidation.validateAndGetLocation(ZWOLLE_001)).thenReturn(location);

        useCase.create(warehouse);

        verify(warehouseValidation).verifyBusinessUnitCodeIsNew("BU1", allWarehouses);
        verify(warehouseValidation).validateAndGetLocation(ZWOLLE_001);
        verify(warehouseValidation).validateCreationFeasibility(location, allWarehouses);
        verify(warehouseValidation).validateCapacityAndStock(location, 100);
        verify(warehouseStore).create(warehouse);
    }

    @Test
    void create_throwsException_whenBusinessUnitCodeNotNew() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        List<DbWarehouse> allWarehouses = Collections.emptyList();
        when(warehouseStore.getAll()).thenReturn(allWarehouses);
        doThrow(new IllegalArgumentException("Duplicate BU code")).when(warehouseValidation)
                .verifyBusinessUnitCodeIsNew(anyString(), anyList());

        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
        verify(warehouseStore, never()).create(any());
    }

    @Test
    void create_throwsException_whenLocationInvalid() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");
        warehouse.setLocation(ZWOLLE_001);
        List<DbWarehouse> allWarehouses = Collections.emptyList();
        when(warehouseStore.getAll()).thenReturn(allWarehouses);
        doThrow(new IllegalArgumentException("Invalid location")).when(warehouseValidation)
                .validateAndGetLocation(ZWOLLE_001);

        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
        verify(warehouseStore, never()).create(any());
    }
}
