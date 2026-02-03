package com.fulfilment.application.monolith.assignment.domain.usecases;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusIntegrationTest
public class FulfilmentAssignmentEndpointIT {

    @Test
    public void testFirstConstraint() {
        // Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
        final String path = "fulfilment-assignment";

        //List all, should have all 3 products the database has initially:
        given()
                .when()
                .body("{\"storeId\": 1,\n\"warehouseBusinessUnitCode\": \"MWH.001\",\n\"productId\": 1}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 1,\n\"warehouseBusinessUnitCode\": \"MWH.012\",\n\"productId\": 1}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 1,\n\"warehouseBusinessUnitCode\": \"MWH.023\",\n\"productId\": 1}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(400)
                .body(containsString("A product can be fulfilled by at most 2 warehouses per store."));
    }

    @Test
    public void testSecondConstraint() {
        //Each Store can be fulfilled by a maximum of 3 different Warehouses
        final String path = "fulfilment-assignment";

        given()
                .when()
                .body("{\"storeId\": 2,\n\"warehouseBusinessUnitCode\": \"MWH.001\",\n\"productId\": 1}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 2,\n\"warehouseBusinessUnitCode\": \"MWH.012\",\n\"productId\": 1}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 2,\n\"warehouseBusinessUnitCode\": \"MWH.023\",\n\"productId\": 2}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 2,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 2}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(400)
                .body(containsString("A storeId can be fulfilled by at most 3 warehouses."));
    }

    @Test
    public void testThirdConstraint() {
        //Each Warehouse can store maximally 5 types of Products
        final String path = "fulfilment-assignment";

        given()
                .when()
                .body("{\"storeId\": 3,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 1}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 3,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 2}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 3,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 3}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 3,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 4}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 3,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 5}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(200);

        given()
                .when()
                .body("{\"storeId\": 3,\n\"warehouseBusinessUnitCode\": \"MWH.025\",\n\"productId\": 6}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(path)
                .then()
                .statusCode(400)
                .body(containsString("A warehouse can store at most 5 types of products."));
    }
}
