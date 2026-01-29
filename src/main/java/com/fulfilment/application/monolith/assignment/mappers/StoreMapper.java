package com.fulfilment.application.monolith.assignment.mappers;

import com.fulfilment.application.monolith.assignment.domain.models.StoreDto;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreMapper {
    public StoreDto toDto(Store store) {
        if (store == null) return null;
        return new StoreDto(
                store.id,
                store.getName(),
                store.getQuantityProductsInStock()
        );
    }

    public Store toEntity(StoreDto dto) {
        if (dto == null) return null;
        Store store = new Store();
        store.id = dto.id();
        store.setName(dto.name());
        store.setQuantityProductsInStock(dto.quantityProductsInStock());
        return store;
    }
}
