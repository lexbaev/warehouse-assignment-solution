package com.fulfilment.application.monolith.assignment.domain.usecases;

import com.fulfilment.application.monolith.assignment.adapters.database.DbFulfilmentAssignment;
import com.fulfilment.application.monolith.assignment.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.assignment.domain.ports.FulfilmentAssignmentOperation;
import com.fulfilment.application.monolith.assignment.mappers.FulfilmentAssignmentMapper;
import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.exceptions.ResourceNotFoundException;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.StoreRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentUseCase implements FulfilmentAssignmentOperation {

    private final UserTransaction userTransaction;

    private final FulfilmentAssignmentRepository fulfilmentAssignmentRepository;

    private final ProductRepository productRepository;

    private final StoreRepository storeRepository;

    private final WarehouseRepository warehouseRepository;

    private final FulfilmentAssignmentMapper fulfilmentAssignmentMapper;

    public FulfilmentAssignmentUseCase(UserTransaction userTransaction, FulfilmentAssignmentRepository fulfilmentAssignmentRepository, ProductRepository productRepository, StoreRepository storeRepository, WarehouseRepository warehouseRepository, FulfilmentAssignmentMapper fulfilmentAssignmentMapper) {
        this.userTransaction = userTransaction;
        this.fulfilmentAssignmentRepository = fulfilmentAssignmentRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.warehouseRepository = warehouseRepository;
        this.fulfilmentAssignmentMapper = fulfilmentAssignmentMapper;
    }

    public FulfilmentAssignmentDto assignWarehouseToProductForStore(Long storeId, String warehouseBusinessUnitCode, Long productId) throws BusinessRuleViolationException {
        try {
            userTransaction.begin();

            if (getCountProductWarehouse(storeId, productId) >= 2) {
                throw new BusinessRuleViolationException("A product can be fulfilled by at most 2 warehouses per store.");
            }

            if (getCountStoreWarehouse(storeId) >= 3 && getCountByStoreIdAndWarehouseBusinessUnitCode(storeId, warehouseBusinessUnitCode) == 0) {
                throw new BusinessRuleViolationException("A storeId can be fulfilled by at most 3 warehouses.");
            }

            if (getWarehouseProductCount(warehouseBusinessUnitCode) >= 5) {
                throw new BusinessRuleViolationException("A warehouse can store at most 5 types of products.");
            }

            FulfilmentAssignmentDto result = persistAndGetFulfilmentAssignment(storeId, productId, warehouseBusinessUnitCode);

            userTransaction.commit();
            return result;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackEx) {
                // Optionally log rollback failure
            }
            if (e instanceof BusinessRuleViolationException) {
                throw (BusinessRuleViolationException) e;
            }
            throw new IllegalStateException("Transaction failed", e);
        }
    }

    public FulfilmentAssignmentDto getAssignmentDtoById(Long assignmentId) {
        DbFulfilmentAssignment assignment = fulfilmentAssignmentRepository.findById(assignmentId);
        if (assignment == null) {
            throw new ResourceNotFoundException("Assignment not found: " + assignmentId);
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
        return fulfilmentAssignmentRepository.countDistinctWarehousesByStoreId(storeId);
    }

    private long getCountByStoreIdAndWarehouseBusinessUnitCode(Long storeId, String warehouseBusinessUnitCode) {
        return fulfilmentAssignmentRepository.countByStoreIdAndWarehouseBusinessUnitCode(storeId, warehouseBusinessUnitCode);
    }

    private long getWarehouseProductCount(String warehouseBusinessUnitCode) {
        return fulfilmentAssignmentRepository.countDistinctProductsByWarehouseBusinessUnitCode(warehouseBusinessUnitCode);
    }

    private FulfilmentAssignmentDto persistAndGetFulfilmentAssignment(Long storeId, Long productId, String warehouseBusinessUnitCode) {
        DbFulfilmentAssignment dbFulfilmentAssignment = new DbFulfilmentAssignment();
        var storeEntity = storeRepository.findById(storeId);
        if (storeEntity == null) {
            throw  new ResourceNotFoundException("Store not found: " + storeId);
        }

        var productEntity = productRepository.findById(productId);
        if (productEntity == null) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }

        var warehouseEntity = warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseBusinessUnitCode));

        dbFulfilmentAssignment.setStore(storeEntity);
        dbFulfilmentAssignment.setProduct(productEntity);
        dbFulfilmentAssignment.setWarehouse(warehouseEntity);
        fulfilmentAssignmentRepository.create(dbFulfilmentAssignment);

        return fulfilmentAssignmentMapper.toDto(dbFulfilmentAssignment);
    }
}