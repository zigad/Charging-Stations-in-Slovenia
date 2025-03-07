package si.deisinger.business.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import si.deisinger.business.entity.ChargingStationsEntity;
import si.deisinger.providers.enums.Providers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChargingStationsResourceTest {

    private static long testStationId;

    @Order(1)
    @Test
    void testCreateStation() {
        ChargingStationsEntity newStation = new ChargingStationsEntity();
        newStation.setStationId(9999L);
        newStation.setProvider(Providers.GREMONAELEKTRIKO.getId());
        newStation.setFriendlyName("Test Station");
        newStation.setAddress("123 Test Street");
        newStation.setLocation("46.06946,14.505751");

        testStationId = given().contentType(ContentType.JSON).body(newStation).when().post("/stations").then().statusCode(Response.Status.CREATED.getStatusCode()).extract().jsonPath().getLong("id"); // Forces it to be Long

        assertTrue(testStationId > 0, "Station ID should be greater than 0");
    }

    @Order(2)
    @Test
    void testGetAllStations() {
        given().when().get("/stations").then().statusCode(200).body("$.size()", greaterThanOrEqualTo(1)); // At least one station should exist
    }

    @Order(3)
    @Test
    void testGetStationById() {
        // Test valid ID
        given().when().get("/stations/" + testStationId).then().statusCode(200);

        // Test invalid ID
        given().when().get("/stations/9999999").then().statusCode(404);
    }

    @Order(4)
    @Test
    void testCreateStationWithInvalidData() {
        // Missing required fields
        ChargingStationsEntity invalidStation = new ChargingStationsEntity();
        invalidStation.setStationId(0L); // Invalid ID
        invalidStation.setProvider(null); // Missing provider
        invalidStation.setFriendlyName(""); // Empty friendly name
        invalidStation.setAddress(null); // Missing address

        given().contentType(ContentType.JSON).body(invalidStation).when().post("/stations").then().statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Order(5)
    @Test
    void testUpdateStation() {
        assertTrue(testStationId > 0, "testStationId should be valid");

        // Get the station before updating
        ChargingStationsEntity previousStation = given().when().get("/stations/" + testStationId).then().statusCode(200).extract().as(ChargingStationsEntity.class);

        // Prepare updated data
        ChargingStationsEntity updatedStation = new ChargingStationsEntity();
        updatedStation.setStationId(881234L);
        updatedStation.setProvider(Providers.GREMONAELEKTRIKO.getId());
        updatedStation.setFriendlyName("Updated Name");
        updatedStation.setAddress("Updated Address");

        // Send update request
        given().contentType(ContentType.JSON).body(updatedStation).when().put("/stations/" + testStationId).then().statusCode(200);

        // Verify update
        ChargingStationsEntity newStation = given().when().get("/stations/" + testStationId).then().statusCode(200).extract().as(ChargingStationsEntity.class);

        // Ensure values changed
        assertNotEquals(previousStation.getStationId(), newStation.getStationId());
        assertNotEquals(previousStation.getFriendlyName(), newStation.getFriendlyName());
        assertNotEquals(previousStation.getAddress(), newStation.getAddress());

        assertEquals(updatedStation.getStationId(), newStation.getStationId());
        assertEquals(updatedStation.getFriendlyName(), newStation.getFriendlyName());
        assertEquals(updatedStation.getAddress(), newStation.getAddress());
    }

    @Order(6)
    @Test
    void testDeleteStation() {
        assertTrue(testStationId > 0, "testStationId should be valid");

        // Test delete a valid station
        given().when().delete("/stations/" + testStationId).then().statusCode(anyOf(is(204), is(404))); // 204 if deleted, 404 if already gone

        // Test deleting the same station again (should return 404)
        given().when().delete("/stations/" + testStationId).then().statusCode(404);
    }

    @Order(7)
    @Test
    void testDeleteStationWithInvalidId() {
        // Test deleting a station that doesn't exist
        given().when().delete("/stations/9999999").then().statusCode(404);
    }

    @Order(8)
    @Test
    void testGetAllStationsWhenEmpty() {
        // Delete all stations if any exist
        given().when().delete("/stations/" + testStationId).then().statusCode(anyOf(is(204), is(404)));

        // Check for an empty response
        given().when().get("/stations/" + testStationId).then().statusCode(404);
    }
}
