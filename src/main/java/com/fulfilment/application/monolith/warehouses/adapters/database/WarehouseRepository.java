package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.DbWarehouseMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  public static final String BUSINESS_UNIT_CODE_1_AND_ARCHIVED_AT_IS_NULL = "businessUnitCode = ?1 and archivedAt is null";
  private final DbWarehouseMapper mapper;

  public WarehouseRepository(DbWarehouseMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<DbWarehouse> getAll() {
    return this.listAll().stream().filter(dbWarehouse -> dbWarehouse.getArchivedAt() == null).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse entity = mapper.toEntity(warehouse);
    persist(entity);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse entity = find(BUSINESS_UNIT_CODE_1_AND_ARCHIVED_AT_IS_NULL, warehouse.getBusinessUnitCode()).firstResult();
    if (entity != null) {
      mapper.updateEntityFromDto(warehouse, entity);
      persist(entity);
    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = find(BUSINESS_UNIT_CODE_1_AND_ARCHIVED_AT_IS_NULL, warehouse.getBusinessUnitCode()).firstResult();
    if (entity != null) {
      entity.setArchivedAt(LocalDateTime.now());
      persist(entity);
    }
  }

  @Override
  public Optional<DbWarehouse> findByBusinessUnitCode(String buCode) {
    return Optional.ofNullable(
            find(BUSINESS_UNIT_CODE_1_AND_ARCHIVED_AT_IS_NULL, buCode).firstResult()
    );
  }

  @Override
  public Optional<DbWarehouse> findById(String id) {
    return Optional.ofNullable(
            find("id", id).firstResult());
  }
}
