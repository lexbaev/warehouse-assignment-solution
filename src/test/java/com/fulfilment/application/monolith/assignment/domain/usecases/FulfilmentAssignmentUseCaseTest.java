package com.fulfilment.application.monolith.assignment.domain.usecases;

import com.fulfilment.application.monolith.assignment.adapters.database.DbFulfilmentAssignment;
import com.fulfilment.application.monolith.assignment.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.assignment.mappers.FulfilmentAssignmentMapper;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FulfilmentAssignmentUseCaseTest {

    private FulfilmentAssignmentRepository assignmentRepository;
    private ProductRepository productRepository;
    private StoreRepository storeRepository;
    private WarehouseRepository warehouseRepository;
    private FulfilmentAssignmentMapper assignmentMapper;
    private FulfilmentAssignmentUseCase useCase;

    @BeforeEach
    void setUp() {
        assignmentRepository = mock(FulfilmentAssignmentRepository.class);
        productRepository = mock(ProductRepository.class);
        storeRepository = mock(StoreRepository.class);
        warehouseRepository = mock(WarehouseRepository.class);
        assignmentMapper = mock(FulfilmentAssignmentMapper.class);
        useCase = new FulfilmentAssignmentUseCase(
                assignmentRepository, productRepository, storeRepository, warehouseRepository, assignmentMapper
        );
    }

    @Test
    void getAssignmentDtoById_returnsDto_whenAssignmentExists() {
        DbFulfilmentAssignment assignment = new DbFulfilmentAssignment();
        FulfilmentAssignmentDto dto = mock(FulfilmentAssignmentDto.class);

        when(assignmentRepository.findById(1L)).thenReturn(assignment);
        when(assignmentMapper.toDto(assignment)).thenReturn(dto);

        FulfilmentAssignmentDto result = useCase.getAssignmentDtoById(1L);

        assertEquals(dto, result);
    }

    @Test
    void getAssignmentDtoById_throws_whenNotFound() {
        when(assignmentRepository.findById(1L)).thenReturn(null);

        assertThrows(WebApplicationException.class, () -> useCase.getAssignmentDtoById(1L));
    }

    @Test
    void assignWarehouseToProductForStore_throws_whenProductWarehouseLimitReached() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(2L);

        assertThrows(WebApplicationException.class, () ->
                useCase.assignWarehouseToProductForStore(1L, "W1", 2L));
    }

    @Test
    void assignWarehouseToProductForStore_throws_whenStoreWarehouseLimitReached() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(0L);
        when(assignmentRepository.findByStoreId(1L)).thenReturn(List.of(mock(DbFulfilmentAssignment.class), mock(DbFulfilmentAssignment.class), mock(DbFulfilmentAssignment.class)));
        when(assignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(1L, "W1")).thenReturn(0L);

        assertThrows(WebApplicationException.class, () ->
                useCase.assignWarehouseToProductForStore(1L, "W1", 2L));
    }

    @Test
    void assignWarehouseToProductForStore_throws_whenWarehouseProductLimitReached() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(0L);
        when(assignmentRepository.findByStoreId(1L)).thenReturn(List.of());
        when(assignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(1L, "W1")).thenReturn(1L);
        when(assignmentRepository.findByWarehouseBusinessUnitCode("W1")).thenReturn(List.of(
                mock(DbFulfilmentAssignment.class),
                mock(DbFulfilmentAssignment.class),
                mock(DbFulfilmentAssignment.class),
                mock(DbFulfilmentAssignment.class),
                mock(DbFulfilmentAssignment.class)
        ));
        when(assignmentRepository.countByWarehouseBusinessUnitCodeAndStoreId("W1", 1L)).thenReturn(0L);

        assertThrows(WebApplicationException.class, () ->
                useCase.assignWarehouseToProductForStore(1L, "W1", 2L));
    }

    @Test
    void assignWarehouseToProductForStore_persistsAndReturnsDto_whenValid() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(0L);
        when(assignmentRepository.findByStoreId(1L)).thenReturn(List.of());
        when(assignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(1L, "W1")).thenReturn(1L);
        when(assignmentRepository.findByWarehouseBusinessUnitCode("W1")).thenReturn(List.of());
        when(assignmentRepository.countByWarehouseBusinessUnitCodeAndStoreId("W1", 1L)).thenReturn(1L);

        Store store = new Store();
        Product product = new Product();
        DbWarehouse warehouse = new DbWarehouse();
        DbFulfilmentAssignment assignment = new DbFulfilmentAssignment();
        FulfilmentAssignmentDto dto = mock(FulfilmentAssignmentDto.class);

        when(storeRepository.findById(1L)).thenReturn(store);
        when(productRepository.findById(2L)).thenReturn(product);
        when(warehouseRepository.findByBusinessUnitCode("W1")).thenReturn(Optional.of(warehouse));
        doAnswer(invocation -> {
            DbFulfilmentAssignment arg = invocation.getArgument(0);
            return arg;
        }).when(assignmentRepository).create(any(DbFulfilmentAssignment.class));
        when(assignmentMapper.toDto(any(DbFulfilmentAssignment.class))).thenReturn(dto);

        FulfilmentAssignmentDto result = useCase.assignWarehouseToProductForStore(1L, "W1", 2L);

        assertEquals(dto, result);
    }

    @Test
    void assignWarehouseToProductForStore_throws_whenStoreNotFound() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(0L);
        when(assignmentRepository.findByStoreId(1L)).thenReturn(List.of());
        when(assignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(1L, "W1")).thenReturn(1L);
        when(assignmentRepository.findByWarehouseBusinessUnitCode("W1")).thenReturn(List.of());
        when(assignmentRepository.countByWarehouseBusinessUnitCodeAndStoreId("W1", 1L)).thenReturn(1L);

        when(storeRepository.findById(1L)).thenReturn(null);

        assertThrows(WebApplicationException.class, () ->
                useCase.assignWarehouseToProductForStore(1L, "W1", 2L));
    }

    @Test
    void assignWarehouseToProductForStore_throws_whenProductNotFound() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(0L);
        when(assignmentRepository.findByStoreId(1L)).thenReturn(List.of());
        when(assignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(1L, "W1")).thenReturn(1L);
        when(assignmentRepository.findByWarehouseBusinessUnitCode("W1")).thenReturn(List.of());
        when(assignmentRepository.countByWarehouseBusinessUnitCodeAndStoreId("W1", 1L)).thenReturn(1L);

        Store store = new Store();
        when(storeRepository.findById(1L)).thenReturn(store);
        when(productRepository.findById(2L)).thenReturn(null);

        assertThrows(WebApplicationException.class, () ->
                useCase.assignWarehouseToProductForStore(1L, "W1", 2L));
    }

    @Test
    void assignWarehouseToProductForStore_throws_whenWarehouseNotFound() {
        when(assignmentRepository.countByStoreIdAndProductId(1L, 2L)).thenReturn(0L);
        when(assignmentRepository.findByStoreId(1L)).thenReturn(List.of());
        when(assignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(1L, "W1")).thenReturn(1L);
        when(assignmentRepository.findByWarehouseBusinessUnitCode("W1")).thenReturn(List.of());
        when(assignmentRepository.countByWarehouseBusinessUnitCodeAndStoreId("W1", 1L)).thenReturn(1L);

        Store store = new Store();
        Product product = new Product();
        when(storeRepository.findById(1L)).thenReturn(store);
        when(productRepository.findById(2L)).thenReturn(product);
        when(warehouseRepository.findByBusinessUnitCode("W1")).thenReturn(Optional.empty());

        assertThrows(WebApplicationException.class, () ->
                useCase.assignWarehouseToProductForStore(1L, "W1", 2L));
    }
}