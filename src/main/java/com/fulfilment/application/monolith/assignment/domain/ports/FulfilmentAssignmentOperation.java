package com.fulfilment.application.monolith.assignment.domain.ports;

import com.fulfilment.application.monolith.assignment.domain.models.FulfilmentAssignmentDto;
import com.fulfilment.application.monolith.exceptions.BusinessRuleViolationException;

import java.util.List;

public interface FulfilmentAssignmentOperation {

    FulfilmentAssignmentDto assignWarehouseToProductForStore(Long storeId, String warehouseBusinessUnitCode, Long productId) throws BusinessRuleViolationException;

    FulfilmentAssignmentDto getAssignmentDtoById(Long assignmentId);

    // for testing purposes only!!!
    List<FulfilmentAssignmentDto> getAllAssignments();
}
