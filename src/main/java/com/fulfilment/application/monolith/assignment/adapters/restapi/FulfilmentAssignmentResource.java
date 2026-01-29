package com.fulfilment.application.monolith.assignment.adapters.restapi;

import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.assignment.domain.ports.FulfilmentAssignmentOperation;
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
        return fulfilmentAssignmentOperation.assignWarehouseToProductForStore(request.storeId(), request.warehouseBusinessUnitCode(), request.productId());
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
