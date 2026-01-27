package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.WarehouseMapper;
import com.warehouse.api.beans.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RetrieveUnitByIdWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private WarehouseMapper mapper;
    private RetrieveUnitByIdWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        mapper = mock(WarehouseMapper.class);
        useCase = new RetrieveUnitByIdWarehouseUseCase(warehouseStore, mapper);
    }

    @Test
    void getById_returnsWarehouse_whenFound() {
        String id = "W1";
        DbWarehouse dbWarehouse = mock(DbWarehouse.class);
        Warehouse apiWarehouse = mock(Warehouse.class);

        when(warehouseStore.findById(id)).thenReturn(Optional.of(dbWarehouse));
        when(mapper.toApiBean(dbWarehouse)).thenReturn(apiWarehouse);

        Warehouse result = useCase.getById(id);

        assertEquals(apiWarehouse, result);
        verify(warehouseStore).findById(id);
        verify(mapper).toApiBean(dbWarehouse);
    }

    @Test
    void getById_returnsNull_whenNotFound() {
        String id = "W2";
        when(warehouseStore.findById(id)).thenReturn(Optional.empty());

        Warehouse result = useCase.getById(id);

        assertNull(result);
        verify(warehouseStore).findById(id);
        verifyNoInteractions(mapper);
    }
}