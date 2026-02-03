package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

    private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

    private final ProductRepository productRepository;

    private UserTransaction userTransaction;

    public ProductResource(ProductRepository productRepository, UserTransaction userTransaction) {
        this.productRepository = productRepository;
        this.userTransaction = userTransaction;
    }

    @GET
    public List<Product> get() {
        return productRepository.listAll(Sort.by("name"));
    }

    @GET
    @Path("{id}")
    public Product getSingle(Long id) {
        Product entity = productRepository.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
        }
        return entity;
    }

    @POST
    public Response create(Product product) {
        if (product.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        try {
            userTransaction.begin();
            productRepository.persist(product);
            userTransaction.commit();
            return Response.ok(product).status(201).build();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception ex) { /* log if needed */ }
            if (e instanceof WebApplicationException) {
                throw (WebApplicationException) e;
            }
            throw new WebApplicationException("Transaction failed", e);
        }
    }

    @PUT
    @Path("{id}")
    public Product update(Long id, Product product) {
        if (product.getName() == null) {
            throw new WebApplicationException("Product Name was not set on request.", 422);
        }
        try {
            userTransaction.begin();
            Product entity = productRepository.findById(id);
            if (entity == null) {
                throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
            }

            // Better to use a mapper here
            entity.setName(product.getName());
            entity.setDescription(product.getDescription());
            entity.setPrice(product.getPrice());
            entity.setStock(product.getStock());

            productRepository.persist(entity);

            userTransaction.commit();
            return entity;
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception ex) { /* log if needed */ }
            if (e instanceof WebApplicationException) {
                throw (WebApplicationException) e;
            }
            throw new WebApplicationException("Transaction failed", e);
        }
    }

    @DELETE
    @Path("{id}")
    public Response delete(Long id) {
        try {
            userTransaction.begin();

            Product entity = productRepository.findById(id);
            if (entity == null) {
                throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
            }
            productRepository.delete(entity);

            userTransaction.commit();
            return Response.status(204).build();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception ex) { /* log if needed */ }
            if (e instanceof WebApplicationException) {
                throw (WebApplicationException) e;
            }
            throw new WebApplicationException("Transaction failed", e);
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