package com.fulfilment.application.monolith.stores;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Store extends PanacheEntity {

  @Column(length = 40, unique = true)
  private String name;

  private int quantityProductsInStock;

  public Store() {}

  public Store(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getQuantityProductsInStock() {
    return quantityProductsInStock;
  }

  public void setQuantityProductsInStock(int quantityProductsInStock) {
    this.quantityProductsInStock = quantityProductsInStock;
  }
}
