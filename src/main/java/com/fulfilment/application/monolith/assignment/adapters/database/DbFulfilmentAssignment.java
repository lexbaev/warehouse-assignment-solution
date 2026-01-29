package com.fulfilment.application.monolith.assignment.adapters.database;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(
        name = "fulfilment_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "product_id", "warehouse_id"})
)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class DbFulfilmentAssignment extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private DbWarehouse warehouse;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public DbWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DbWarehouse dbWarehouse) {
        this.warehouse = dbWarehouse;
    }
}
