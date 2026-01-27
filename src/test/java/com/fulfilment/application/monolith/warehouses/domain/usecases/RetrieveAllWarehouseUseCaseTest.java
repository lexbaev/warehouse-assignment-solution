package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.WarehouseMapper;
import com.warehouse.api.beans.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RetrieveAllWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private WarehouseMapper mapper;
    private RetrieveAllWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        mapper = mock(WarehouseMapper.class);
        useCase = new RetrieveAllWarehouseUseCase(warehouseStore, mapper);
    }

    @Test
    void getAll_returnsMappedWarehouses() {
        DbWarehouse dbWarehouse1 = mock(DbWarehouse.class);
        DbWarehouse dbWarehouse2 = mock(DbWarehouse.class);
        Warehouse apiWarehouse1 = mock(Warehouse.class);
        Warehouse apiWarehouse2 = mock(Warehouse.class);

        when(warehouseStore.getAll()).thenReturn(List.of(dbWarehouse1, dbWarehouse2));
        when(mapper.toApiBean(dbWarehouse1)).thenReturn(apiWarehouse1);
        when(mapper.toApiBean(dbWarehouse2)).thenReturn(apiWarehouse2);

        List<Warehouse> result = useCase.getAll();

        assertEquals(List.of(apiWarehouse1, apiWarehouse2), result);
        verify(warehouseStore).getAll();
        verify(mapper).toApiBean(dbWarehouse1);
        verify(mapper).toApiBean(dbWarehouse2);
    }

    @Test
    void getAll_returnsEmptyList_whenNoWarehouses() {
        when(warehouseStore.getAll()).thenReturn(List.of());

        List<Warehouse> result = useCase.getAll();

        assertEquals(List.of(), result);
        verify(warehouseStore).getAll();
        verifyNoInteractions(mapper);
    }
}