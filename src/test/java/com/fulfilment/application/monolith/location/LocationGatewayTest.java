package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class LocationGatewayTest {

    @Test
    public void testWhenResolveExistingLocationShouldReturn() {
        // given
        LocationGateway locationGateway = new LocationGateway();

        // when
        Optional<Location> locationOpt = locationGateway.resolveByIdentifier("ZWOLLE-001");

        // then
        assertTrue(locationOpt.isPresent());
        assertEquals("ZWOLLE-001", locationOpt.get().identification());
    }

    @Test
    public void testWhenResolveNonExistingLocationShouldReturnEmpty() {
        // given
        LocationGateway locationGateway = new LocationGateway();

        // when
        Optional<Location> locationOpt = locationGateway.resolveByIdentifier("NON-EXISTENT-ID");

        // then
        assertFalse(locationOpt.isPresent());
    }
}
