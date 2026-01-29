package com.fulfilment.application.monolith.assignment.mappers;

import com.fulfilment.application.monolith.assignment.adapters.database.DbFulfilmentAssignment;
import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.assignment.domain.models.ProductDto;
import com.fulfilment.application.monolith.assignment.domain.models.StoreDto;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.mappers.DbWarehouseMapper;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FulfilmentAssignmentMapper {

    private final DbWarehouseMapper dbWarehouseMapper;
    private final StoreMapper storeMapper;
    private final ProductMapper productMapper;

    public FulfilmentAssignmentMapper(
            DbWarehouseMapper dbWarehouseMapper,
            StoreMapper storeMapper,
            ProductMapper productMapper
    ) {
        this.dbWarehouseMapper = dbWarehouseMapper;
        this.storeMapper = storeMapper;
        this.productMapper = productMapper;
    }

    public FulfilmentAssignmentDto toDto(DbFulfilmentAssignment entity) {
        if (entity == null) return null;
        StoreDto storeDto = storeMapper.toDto(entity.getStore());
        ProductDto productDto = productMapper.toDto(entity.getProduct());
        Warehouse warehouse = dbWarehouseMapper.toDomain(entity.getWarehouse());
        return new FulfilmentAssignmentDto(entity.id, storeDto, productDto, warehouse);
    }

    public DbFulfilmentAssignment toEntity(FulfilmentAssignmentDto dto) {
        if (dto == null) return null;
        DbFulfilmentAssignment entity = new DbFulfilmentAssignment();
        Store store = storeMapper.toEntity(dto.store());
        Product product = productMapper.toEntity(dto.product());
        DbWarehouse dbWarehouse = dbWarehouseMapper.toEntity(dto.warehouse());
        entity.setStore(store);
        entity.setProduct(product);
        entity.setWarehouse(dbWarehouse);
        return entity;
    }
}
