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

    public List<DbFulfilmentAssignment> findByStoreId(Long storeId) {
        return list("store.id = ?1", storeId);
    }

    public List<DbFulfilmentAssignment> findByWarehouseBusinessUnitCode(String warehouseBusinessUnitCode) {
        return list("warehouse.businessUnitCode = ?1 and warehouse.archivedAt is null", warehouseBusinessUnitCode);
    }

    public void create(DbFulfilmentAssignment assignment) {
        persist(assignment);
    }

    public long countDistinctWarehousesByStoreId(Long storeId) {
        return getEntityManager()
                .createQuery(
                        "select count(distinct a.warehouse.businessUnitCode) from DbFulfilmentAssignment a where a.store.id = :storeId and a.warehouse.archivedAt is null",
                        Long.class
                )
                .setParameter("storeId", storeId)
                .getSingleResult();
    }

    public long countDistinctProductsByWarehouseBusinessUnitCode(String warehouseBusinessUnitCode) {
        return getEntityManager()
                .createQuery(
                        "select count(distinct a.product.id) from DbFulfilmentAssignment a where a.warehouse.businessUnitCode = :warehouseBusinessUnitCode and a.warehouse.archivedAt is null",
                        Long.class
                )
                .setParameter("warehouseBusinessUnitCode", warehouseBusinessUnitCode)
                .getSingleResult();
    }
}
