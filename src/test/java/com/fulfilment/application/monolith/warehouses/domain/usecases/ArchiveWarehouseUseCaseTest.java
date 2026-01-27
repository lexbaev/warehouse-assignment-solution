package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ArchiveWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        useCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    void archive_removesWarehouse_whenFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU1");

        when(warehouseStore.findByBusinessUnitCode("BU1")).thenReturn(Optional.of(mock(DbWarehouse.class)));

        useCase.archive(warehouse);

        verify(warehouseStore).remove(warehouse);
    }

    @Test
    void archive_throwsException_whenNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("BU2");

        when(warehouseStore.findByBusinessUnitCode("BU2")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.archive(warehouse));
        assertTrue(ex.getMessage().contains("Any active warehouse with business unit code BU2 is not found."));
        verify(warehouseStore, never()).remove(any());
    }
}