package com.fulfilment.application.monolith.assignment.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentRepository implements PanacheRepository<DbFulfilmentAssignment> {

    public long countByStoreIdAndProductId(Long storeId, Long productId) {
        return count("store.id = ?1 and product.id = ?2", storeId, productId);
    }

    public long countByStoreIdAndWarehouseBusinessUnitCode(Long storeId, String warehouseBusinessUnitCode) {
        return count("store.id = ?1 and warehouse.businessUnitCode = ?2 and warehouse.archivedAt is null", storeId, warehouseBusinessUnitCode);
    }

    public long countByWarehouseId(String warehouseId) {
        return count("warehouse.businessUnitCode = ?1 and warehouse.archivedAt is null", warehouseId);
    }

    public long countByWarehouseBusinessUnitCodeAndStoreId(String warehouseBusinessUnitCode, Long storeId) {
        return count("warehouse.businessUnitCode = ?1 and warehouse.archivedAt is null and store.id = ?2", warehouseBusinessUnitCode, storeId);
    }

    public List<DbFulfilmentAssignment> findByStoreId(Long storeId) {
        return list("store.id = ?1", storeId);
    }

    public List<DbFulfilmentAssignment> findByWarehouseBusinessUnitCode(String warehouseBusinessUnitCode) {
        return list("warehouse.businessUnitCode = ?1 and warehouse.archivedAt is null", warehouseBusinessUnitCode);
    }

    public List<DbFulfilmentAssignment> findByProductId(Long productId) {
        return list("product.id = ?1", productId);
    }

    public void create(DbFulfilmentAssignment assignment) {
        persist(assignment);
    }
}
