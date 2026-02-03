package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.warehouses.domain.ports.*;
import com.fulfilment.application.monolith.warehouses.mappers.WarehouseMapper;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private final RetrieveAllWarehouseOperation retrieveAllWarehouseOperation;

  private final CreateWarehouseOperation createWarehouseOperation;

  private final ReplaceWarehouseOperation replaceWarehouseOperation;

  private final ArchiveWarehouseOperation archiveWarehouseOperation;

  private final RetrieveUnitByIdWarehouseOperation retrieveUnitByIdWarehouseOperation;

  private final WarehouseMapper mapper;

    public WarehouseResourceImpl(RetrieveAllWarehouseOperation retrieveAllWarehouseOperation, CreateWarehouseOperation createWarehouseOperation, ReplaceWarehouseOperation replaceWarehouseOperation, ArchiveWarehouseOperation archiveWarehouseOperation, RetrieveUnitByIdWarehouseOperation retrieveUnitByIdWarehouseOperation, WarehouseMapper mapper) {
        this.retrieveAllWarehouseOperation = retrieveAllWarehouseOperation;
        this.createWarehouseOperation = createWarehouseOperation;
        this.replaceWarehouseOperation = replaceWarehouseOperation;
        this.archiveWarehouseOperation = archiveWarehouseOperation;
        this.retrieveUnitByIdWarehouseOperation = retrieveUnitByIdWarehouseOperation;
        this.mapper = mapper;
    }

    @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return retrieveAllWarehouseOperation.getAll();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
        createWarehouseOperation.create(mapper.toDomainModel(data));
    } catch (BusinessRuleViolationException e) {
        throw new WebApplicationException(e.getMessage(), 400);
    }
    return data;
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    return retrieveUnitByIdWarehouseOperation.getById(id);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    Warehouse warehouse = retrieveUnitByIdWarehouseOperation.getById(id);
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with ID " + id + " not found.", 404);
    }
    try {
      archiveWarehouseOperation.archive(mapper.toDomainModel(warehouse));
    } catch (BusinessRuleViolationException e) {
        throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    try {
      replaceWarehouseOperation.replace(mapper.toDomainModel(data));
    } catch (BusinessRuleViolationException e) {
        throw new WebApplicationException(e.getMessage(), 400);
    }
    return data;
  }
}
