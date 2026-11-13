package br.com.fiap.phase4.workorder.iandt;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Customer and Vehicle Controller Integration Tests")
class CustomerVehicleControllerIntegrationTest extends WorkOrderIntegrationTest {

    @Nested
    @DisplayName("Customer Lifecycle API Scenarios")
    class CustomerLifecycleScenarios {

        @Test
        @DisplayName("Should successfully create, retrieve by ID, and list customer by document")
        void shouldCreateAndRetrieveCustomer() {
            String document = "52998224725";
            String email = "customer." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String name = "Integration Customer";

            Map<String, Object> request = Map.of(
                    "name", name,
                    "email", email,
                    "document", document
            );

            String customerId = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo(name))
                    .body("email", equalTo(email))
                    .body("document", equalTo(document))
                    .extract()
                    .path("id");

            given()
                    .when()
                    .get("/api/v1/customers/{id}", customerId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(customerId))
                    .body("name", equalTo(name))
                    .body("document", equalTo(document));

            given()
                    .queryParam("document", document)
                    .when()
                    .get("/api/v1/customers")
                    .then()
                    .statusCode(200)
                    .body("content", not(empty()))
                    .body("content[0].document", equalTo(document));
        }

        @Test
        @DisplayName("Should update customer profile information via PATCH")
        void shouldUpdateCustomerViaPatch() {
            String document = "63846452114";
            String email = "update." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

            String customerId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Original Name", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Updated Name", "email", "new." + email))
                    .when()
                    .patch("/api/v1/customers/{id}", customerId)
                    .then()
                    .statusCode(200)
                    .body("name", equalTo("Updated Name"))
                    .body("email", equalTo("new." + email));
        }

        @Test
        @DisplayName("Should reject customer creation when document is invalid")
        void shouldRejectInvalidDocument() {
            Map<String, Object> request = Map.of(
                    "name", "Invalid CPF Customer",
                    "email", "invalid@example.com",
                    "document", "123456"
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(400), is(422)));
        }

        @Test
        @DisplayName("Should reject duplicate customer creation with identical document")
        void shouldRejectDuplicateCustomerDocument() {
            String document = "67429469540";
            Map<String, Object> request = Map.of(
                    "name", "First Instance Customer",
                    "email", "first." + UUID.randomUUID().toString().substring(0, 6) + "@test.com",
                    "document", document
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(201);

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "name", "Second Instance Customer",
                            "email", "second." + UUID.randomUUID().toString().substring(0, 6) + "@test.com",
                            "document", document
                    ))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(400), is(409), is(422)));
        }

        @Test
        @DisplayName("Should safely delete customer without vehicles by ID")
        void shouldDeleteCustomerWithoutVehiclesSafely() {
            String document = "95786318478";
            String customerId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "name", "Customer To Delete",
                            "email", "delete." + UUID.randomUUID().toString().substring(0, 6) + "@test.com",
                            "document", document
                    ))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            given()
                    .when()
                    .delete("/api/v1/customers/{id}", customerId)
                    .then()
                    .statusCode(204);
        }

        @Test
        @DisplayName("Should reject customer deletion when vehicles are associated (RFC 7807 422)")
        void shouldRejectCustomerDeletionWhenVehiclesExist() {
            String document = "11144477735";
            String email = "owner." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

            String customerId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Vehicle Owner Client", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", "CAR1A23",
                            "make", "Toyota",
                            "model", "Yaris",
                            "manufactureYear", 2023
                    ))
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(201);

            // Attempting to delete customer with vehicle must return 422
            given()
                    .when()
                    .delete("/api/v1/customers/{id}", customerId)
                    .then()
                    .statusCode(422)
                    .body("errorCode", equalTo("CUSTOMER_HAS_VEHICLES"));
        }
    }

    @Nested
    @DisplayName("Vehicle Lifecycle API Scenarios")
    class VehicleLifecycleScenarios {

        @Test
        @DisplayName("Should successfully create vehicle linked to customer and retrieve by plate")
        void shouldCreateAndRetrieveVehicle() {
            String document = "71428793860";
            String email = "vehicle.owner." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Vehicle Owner", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            String licensePlate = "BRA2E19";
            Map<String, Object> vehicleRequest = Map.of(
                    "customerDocument", document,
                    "licensePlate", licensePlate,
                    "make", "Toyota",
                    "model", "Corolla",
                    "manufactureYear", 2024
            );

            String vehicleId = given()
                    .contentType(ContentType.JSON)
                    .body(vehicleRequest)
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("licensePlate", equalTo(licensePlate))
                    .extract()
                    .path("id");

            given()
                    .when()
                    .get("/api/v1/vehicles/{id}", vehicleId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(vehicleId))
                    .body("licensePlate", equalTo(licensePlate))
                    .body("model", equalTo("Corolla"));

            given()
                    .queryParam("licensePlate", licensePlate)
                    .when()
                    .get("/api/v1/vehicles")
                    .then()
                    .statusCode(200)
                    .body("content", not(empty()))
                    .body("content[0].licensePlate", equalTo(licensePlate));
        }

        @Test
        @DisplayName("Should reject vehicle creation when customer is not found")
        void shouldRejectVehicleCreationForNonExistingCustomer() {
            Map<String, Object> vehicleRequest = Map.of(
                    "customerDocument", "99999999999",
                    "licensePlate", "NFD1A23",
                    "make", "Ford",
                    "model", "Ka",
                    "manufactureYear", 2020
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(vehicleRequest)
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(anyOf(is(400), is(404), is(422)));
        }

        @Test
        @DisplayName("Should safely delete vehicle by ID")
        void shouldDeleteVehicleSafely() {
            String document = "66202667036";
            String email = "del.veh." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Vehicle Delete Client", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            String vehicleId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", "DEL1A23",
                            "make", "Fiat",
                            "model", "Uno",
                            "manufactureYear", 2019
                    ))
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            given()
                    .when()
                    .delete("/api/v1/vehicles/{id}", vehicleId)
                    .then()
                    .statusCode(204);
        }
    }
}
