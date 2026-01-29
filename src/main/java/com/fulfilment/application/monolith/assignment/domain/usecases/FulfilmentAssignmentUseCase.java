package com.fulfilment.application.monolith.assignment.domain.usecases;

import com.fulfilment.application.monolith.assignment.adapters.database.DbFulfilmentAssignment;
import com.fulfilment.application.monolith.assignment.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.assignment.domain.ports.FulfilmentAssignmentOperation;
import com.fulfilment.application.monolith.assignment.mappers.FulfilmentAssignmentMapper;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentUseCase implements FulfilmentAssignmentOperation {

    private final FulfilmentAssignmentRepository fulfilmentAssignmentRepository;

    private final ProductRepository productRepository;

    private final StoreRepository storeRepository;

    private final WarehouseRepository warehouseRepository;

    private final FulfilmentAssignmentMapper fulfilmentAssignmentMapper;

    public FulfilmentAssignmentUseCase(FulfilmentAssignmentRepository fulfilmentAssignmentRepository, ProductRepository productRepository, StoreRepository storeRepository, WarehouseRepository warehouseRepository, FulfilmentAssignmentMapper fulfilmentAssignmentMapper) {
        this.fulfilmentAssignmentRepository = fulfilmentAssignmentRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.warehouseRepository = warehouseRepository;
        this.fulfilmentAssignmentMapper = fulfilmentAssignmentMapper;
    }

    @Transactional
    public FulfilmentAssignmentDto assignWarehouseToProductForStore(Long storeId, String warehouseBusinessUnitCode, Long productId) {
        // Constraint 1: Each Product can be fulfilled by max 2 Warehouses per Store
        if (getCountProductWarehouse(storeId, productId) >= 2) {
            throw new WebApplicationException("A product can be fulfilled by at most 2 warehouses per store.", 404);
        }

        // Constraint 2: Each Store can be fulfilled by max 3 Warehouses
        if (getCountStoreWarehouse(storeId) >= 3 && getCountByStoreIdAndWarehouseBusinessUnitCode(storeId, warehouseBusinessUnitCode) == 0) {
            throw new WebApplicationException("A storeId can be fulfilled by at most 3 warehouses.", 404);
        }

        // Constraint 3: Each Warehouse can store max 5 types of Products
        if (getWarehouseProductCount(warehouseBusinessUnitCode) >= 5) {
            throw new WebApplicationException("A warehouse can store at most 5 types of products.", 404);
        }

        return persistAndGetFulfilmentAssignment(storeId, productId, warehouseBusinessUnitCode);
    }

    public FulfilmentAssignmentDto getAssignmentDtoById(Long assignmentId) {
        DbFulfilmentAssignment assignment = fulfilmentAssignmentRepository.findById(assignmentId);
        if (assignment == null) {
            throw new WebApplicationException("Assignment not found: " + assignmentId, 404);
        }
        return fulfilmentAssignmentMapper.toDto(assignment);
    }

    // for testing purposes only!!!
    public List<FulfilmentAssignmentDto> getAllAssignments() {
        return fulfilmentAssignmentRepository.findAll().stream()
                .map(fulfilmentAssignmentMapper::toDto)
                .toList();
    }

    private long getCountProductWarehouse(Long storeId, Long productId) {
        return fulfilmentAssignmentRepository.countByStoreIdAndProductId(storeId, productId);
    }

    private long getCountStoreWarehouse(Long storeId) {
        return fulfilmentAssignmentRepository.findByStoreId(storeId).stream()
                .map(DbFulfilmentAssignment::getWarehouse)
                .distinct()
                .count();
    }

    private long getCountByStoreIdAndWarehouseBusinessUnitCode(Long storeId, String warehouseBusinessUnitCode) {
        return fulfilmentAssignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(storeId, warehouseBusinessUnitCode);
    }

    private long getWarehouseProductCount(String warehouseBusinessUnitCode) {
        return fulfilmentAssignmentRepository.findByWarehouseBusinessUnitCode(warehouseBusinessUnitCode).stream()
                .map(DbFulfilmentAssignment::getProduct)
                .distinct()
                .count();
    }

    private FulfilmentAssignmentDto persistAndGetFulfilmentAssignment(Long storeId, Long productId, String warehouseBusinessUnitCode) {
        DbFulfilmentAssignment dbFulfilmentAssignment = new DbFulfilmentAssignment();
        var storeEntity = storeRepository.findById(storeId);
        if (storeEntity == null) {
            throw  new WebApplicationException("Store not found: " + storeId, 404);
        }

        var productEntity = productRepository.findById(productId);
        if (productEntity == null) {
            throw new WebApplicationException("Product not found: " + productId, 404);
        }

        var warehouseEntity = warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode)
                .orElseThrow(() -> new WebApplicationException("Warehouse not found: " + warehouseBusinessUnitCode, 404));

        dbFulfilmentAssignment.setStore(storeEntity);
        dbFulfilmentAssignment.setProduct(productEntity);
        dbFulfilmentAssignment.setWarehouse(warehouseEntity);
        fulfilmentAssignmentRepository.create(dbFulfilmentAssignment);

        return fulfilmentAssignmentMapper.toDto(dbFulfilmentAssignment);
    }
}