package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  private final LegacyStoreManagerGateway legacyStoreManagerGateway;

  private final UserTransaction userTransaction;

  public StoreResource(LegacyStoreManagerGateway legacyStoreManagerGateway, UserTransaction userTransaction) {
    this.legacyStoreManagerGateway = legacyStoreManagerGateway;
    this.userTransaction = userTransaction;
  }

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    try {
      userTransaction.begin();
      store.persist();
      userTransaction.commit();

      // Only call legacy system after successful commit
      legacyStoreManagerGateway.createStoreOnLegacySystem(store);

      return Response.ok(store).status(201).build();
    } catch (Exception e) {
      try {
        if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
          userTransaction.rollback();
        }
      } catch (Exception rollbackEx) {
        // log rollback exception if needed
      }
      throw new WebApplicationException("Transaction failed: " + e.getMessage(), 500);
    }
  }

  @PUT
  @Path("{id}")
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.getName() == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }
    try {
      userTransaction.begin();
      Store entity = Store.findById(id);

      if (entity == null) {
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }
      
      // Better to use mappers for this
      entity.setName(updatedStore.getName());
      entity.setQuantityProductsInStock(updatedStore.getQuantityProductsInStock());
      userTransaction.commit();

      legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

      return entity;
    } catch (Exception e) {
      try {
        if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
          userTransaction.rollback();
        }
      } catch (Exception rollbackEx) {
        // log rollback exception if needed
      }
      throw new WebApplicationException("Transaction failed: " + e.getMessage(), 500);
    }
  }

  @PATCH
  @Path("{id}")
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.getName() == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    try {
      userTransaction.begin();
      Store entity = Store.findById(id);

      if (entity == null) {
        throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
      }

      if (updatedStore.getName() != null) {
        entity.setName(updatedStore.getName());
      }

      if (updatedStore.getQuantityProductsInStock() != 0) {
        entity.setQuantityProductsInStock(updatedStore.getQuantityProductsInStock());
      }

      userTransaction.commit();

      legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

      return entity;
    } catch (Exception e) {
      try {
        if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
          userTransaction.rollback();
        }
      } catch (Exception rollbackEx) {
        // log rollback exception if needed
      }
      throw new WebApplicationException("Transaction failed: " + e.getMessage(), 500);
    }
  }

  @DELETE
  @Path("{id}")
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    try {
      userTransaction.begin();
      entity.delete();
      userTransaction.commit();

      legacyStoreManagerGateway.deleteStoreOnLegacySystem(id);

      return Response.status(204).build();
    } catch (Exception e) {
      try {
        if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
          userTransaction.rollback();
        }
      } catch (Exception rollbackEx) {
        // log rollback exception if needed
      }
      throw new WebApplicationException("Transaction failed: " + e.getMessage(), 500);
    }
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      // I would recommend to create a proper Error Response DTO class instead of building JSON manually
      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
