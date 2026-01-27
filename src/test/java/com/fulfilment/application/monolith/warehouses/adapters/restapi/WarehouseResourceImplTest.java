package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.*;
import com.fulfilment.application.monolith.warehouses.mappers.WarehouseMapper;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WarehouseResourceImplTest {

    private RetrieveAllWarehouseOperation retrieveAllWarehouseOperation;
    private CreateWarehouseOperation createWarehouseOperation;
    private ReplaceWarehouseOperation replaceWarehouseOperation;
    private ArchiveWarehouseOperation archiveWarehouseOperation;
    private RetrieveUnitByIdWarehouseOperation retrieveUnitByIdWarehouseOperation;
    private WarehouseMapper mapper;
    private WarehouseResourceImpl resource;

    @BeforeEach
    void setUp() {
        retrieveAllWarehouseOperation = mock(RetrieveAllWarehouseOperation.class);
        createWarehouseOperation = mock(CreateWarehouseOperation.class);
        replaceWarehouseOperation = mock(ReplaceWarehouseOperation.class);
        archiveWarehouseOperation = mock(ArchiveWarehouseOperation.class);
        retrieveUnitByIdWarehouseOperation = mock(RetrieveUnitByIdWarehouseOperation.class);
        mapper = mock(WarehouseMapper.class);
        resource = new WarehouseResourceImpl(
                retrieveAllWarehouseOperation,
                createWarehouseOperation,
                replaceWarehouseOperation,
                archiveWarehouseOperation,
                retrieveUnitByIdWarehouseOperation,
                mapper
        );
    }

    @Test
    void listAllWarehousesUnits_returnsList() {
        Warehouse w1 = mock(Warehouse.class);
        Warehouse w2 = mock(Warehouse.class);
        when(retrieveAllWarehouseOperation.getAll()).thenReturn(List.of(w1, w2));

        List<Warehouse> result = resource.listAllWarehousesUnits();

        assertEquals(List.of(w1, w2), result);
        verify(retrieveAllWarehouseOperation).getAll();
    }

    @Test
    void createANewWarehouseUnit_delegatesToOperationAndReturnsInput() {
        Warehouse apiWarehouse = mock(Warehouse.class);
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
                mock(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);

        when(mapper.toDomainModel(apiWarehouse)).thenReturn(domainWarehouse);

        Warehouse result = resource.createANewWarehouseUnit(apiWarehouse);

        assertEquals(apiWarehouse, result);
        verify(createWarehouseOperation).create(domainWarehouse);
    }

    @Test
    void getAWarehouseUnitByID_returnsWarehouse() {
        Warehouse apiWarehouse = mock(Warehouse.class);
        when(retrieveUnitByIdWarehouseOperation.getById("id1")).thenReturn(apiWarehouse);

        Warehouse result = resource.getAWarehouseUnitByID("id1");

        assertEquals(apiWarehouse, result);
        verify(retrieveUnitByIdWarehouseOperation).getById("id1");
    }

    @Test
    void archiveAWarehouseUnitByID_throws404_whenNotFound() {
        when(retrieveUnitByIdWarehouseOperation.getById("id2")).thenReturn(null);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("id2"));
        assertEquals(404, ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("Warehouse with ID id2 not found."));
        verifyNoInteractions(archiveWarehouseOperation);
    }

    @Test
    void archiveAWarehouseUnitByID_delegatesToOperation_whenFound() {
        Warehouse apiWarehouse = mock(Warehouse.class);
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
                mock(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);

        when(retrieveUnitByIdWarehouseOperation.getById("id3")).thenReturn(apiWarehouse);
        when(mapper.toDomainModel(apiWarehouse)).thenReturn(domainWarehouse);

        resource.archiveAWarehouseUnitByID("id3");

        verify(archiveWarehouseOperation).archive(domainWarehouse);
    }

    @Test
    void replaceTheCurrentActiveWarehouse_delegatesToOperationAndReturnsInput() {
        Warehouse apiWarehouse = mock(Warehouse.class);
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
                mock(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);

        when(mapper.toDomainModel(apiWarehouse)).thenReturn(domainWarehouse);

        Warehouse result = resource.replaceTheCurrentActiveWarehouse("BU1", apiWarehouse);

        assertEquals(apiWarehouse, result);
        verify(replaceWarehouseOperation).replace(domainWarehouse);
    }
}