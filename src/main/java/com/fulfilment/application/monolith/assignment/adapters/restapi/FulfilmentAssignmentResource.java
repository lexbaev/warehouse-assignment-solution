package com.fulfilment.application.monolith.assignment.adapters.restapi;

import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.assignment.domain.ports.FulfilmentAssignmentOperation;
import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;
import com.fulfilment.application.monolith.exceptions.ResourceNotFoundException;
import io.smallrye.common.constraint.NotNull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;

import java.util.List;

@Path("fulfilment-assignment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentAssignmentResource {

    private final FulfilmentAssignmentOperation fulfilmentAssignmentOperation;

    public FulfilmentAssignmentResource(FulfilmentAssignmentOperation fulfilmentAssignmentOperation) {
        this.fulfilmentAssignmentOperation = fulfilmentAssignmentOperation;
    }

    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public FulfilmentAssignmentDto assignWarehouseToProductForStore(@NotNull FulfilmentAssignmentRequest request) {
        try {
            return fulfilmentAssignmentOperation.assignWarehouseToProductForStore(request.storeId(), request.warehouseBusinessUnitCode(), request.productId());
        } catch (BusinessRuleViolationException e) {
            throw new WebApplicationException(e.getMessage(), 400);
        } catch (ResourceNotFoundException e) {
            throw new WebApplicationException("Resource not found", 404);
        }
    }

    @Path("/{id}")
    @GET
    @Produces("application/json")
    public FulfilmentAssignmentDto getAssignmentDtoById(@PathParam("id") Long assignmentId) {
        return fulfilmentAssignmentOperation.getAssignmentDtoById(assignmentId);
    }

    // for testing purposes only!!!
    @Path("/all")
    @GET
    @Produces("application/json")
    public List<FulfilmentAssignmentDto> getAllAssignments() {
        return fulfilmentAssignmentOperation.getAllAssignments();
    }
}
