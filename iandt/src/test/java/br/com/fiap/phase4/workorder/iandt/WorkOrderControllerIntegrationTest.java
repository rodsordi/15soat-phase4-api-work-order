package br.com.fiap.phase4.workorder.iandt;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Work Order Controller REST Integration Tests")
class WorkOrderControllerIntegrationTest extends WorkOrderIntegrationTest {

    @Nested
    @DisplayName("Work Order REST Operations")
    class WorkOrderOperations {

        @Test
        @DisplayName("Should create work order and follow complete 8-phase lifecycle through to DELIVERED")
        void shouldFollowFullWorkOrderLifecycleToDelivered() {
            String document = "71428793860";
            String email = "wo.owner." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String plate = "KGB1A23";

            // 1. Create prerequisite Customer and Vehicle
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Work Order Client", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "make", "Honda",
                            "model", "Civic",
                            "manufactureYear", 2023
                    ))
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            // 2. Phase 1: RECEIVED
            Map<String, Object> createOrderRequest = Map.of(
                    "customerDocument", document,
                    "licensePlate", plate,
                    "description", "Complete engine and brake maintenance"
            );

            String orderId = given()
                    .contentType(ContentType.JSON)
                    .body(createOrderRequest)
                    .when()
                    .post("/api/v1/work-orders")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("status", equalTo("RECEIVED"))
                    .extract()
                    .path("id");

            // 3. Phase 2: DIAGNOSING
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "DIAGNOSING", "reason", "Diagnostic started by mechanic"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("DIAGNOSING"));

            // 4. Phase 3: WAITING_APPROVAL
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "WAITING_APPROVAL", "reason", "Diagnostic completed, budget ready"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("WAITING_APPROVAL"));

            // 5. Phase 4: APPROVED
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "APPROVED", "reason", "Budget approved by customer"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("APPROVED"));

            // 6. Phase 5: EXECUTING
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "EXECUTING", "reason", "Mechanical repair started"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("EXECUTING"));

            // 7. Phase 6: COMPLETED
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "COMPLETED", "reason", "Mechanical repair completed"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED"));

            // 8. Phase 7: PAID
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "PAID", "reason", "Payment confirmed via Mercado Pago"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("PAID"));

            // 9. Phase 8: DELIVERED
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "DELIVERED", "reason", "Vehicle delivered to customer"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("DELIVERED"));

            // 10. List filtered by status DELIVERED
            given()
                    .queryParam("status", "DELIVERED")
                    .when()
                    .get("/api/v1/work-orders")
                    .then()
                    .statusCode(200)
                    .body("content", not(empty()));
        }

        @Test
        @DisplayName("Should cancel work order when customer rejects budget at WAITING_APPROVAL")
        void shouldCancelWorkOrderUponCustomerBudgetRejection() {
            String document = "66202667036";
            String email = "reject.wo." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String plate = "REJ1A23";

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Budget Rejection Client", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "make", "Fiat",
                            "model", "Argo",
                            "manufactureYear", 2022
                    ))
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            String orderId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "description", "Budget rejection flow"
                    ))
                    .when()
                    .post("/api/v1/work-orders")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "WAITING_APPROVAL", "reason", "Diagnosis completed, budget ready"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("WAITING_APPROVAL"));

            // Customer rejects budget -> transitions to CANCELED
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "CANCELED", "reason", "Budget rejected by customer"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("CANCELED"));
        }

        @Test
        @DisplayName("Should reject vehicle delivery before payment is confirmed (COMPLETED to DELIVERED)")
        void shouldRejectDeliveryBeforePayment() {
            String document = "99696599528";
            String email = "premature.del." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String plate = "PRE1A23";

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Delivery Test Client", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "make", "Chevrolet",
                            "model", "Onix",
                            "manufactureYear", 2023
                    ))
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            String orderId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "description", "Premature delivery test"
                    ))
                    .when()
                    .post("/api/v1/work-orders")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            // Advance to COMPLETED
            given().contentType(ContentType.JSON).body(Map.of("status", "WAITING_APPROVAL", "reason", "Ready")).when().patch("/api/v1/work-orders/{id}", orderId).then().statusCode(200);
            given().contentType(ContentType.JSON).body(Map.of("status", "APPROVED", "reason", "Approved")).when().patch("/api/v1/work-orders/{id}", orderId).then().statusCode(200);
            given().contentType(ContentType.JSON).body(Map.of("status", "EXECUTING", "reason", "Executing")).when().patch("/api/v1/work-orders/{id}", orderId).then().statusCode(200);
            given().contentType(ContentType.JSON).body(Map.of("status", "COMPLETED", "reason", "Done")).when().patch("/api/v1/work-orders/{id}", orderId).then().statusCode(200);

            // Attempting to deliver before payment confirmation must fail with 400/422
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "DELIVERED", "reason", "Attempting delivery without payment"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(anyOf(is(400), is(422)));
        }

        @Test
        @DisplayName("Should reject invalid state transition according to domain state machine invariants")
        void shouldRejectInvalidStateTransition() {
            String document = "63846452114";
            String email = "invalid.transition." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
            String plate = "XYZ9A87";

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Transition Test Client", "email", email, "document", document))
                    .when()
                    .post("/api/v1/customers")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "make", "Ford",
                            "model", "Focus",
                            "manufactureYear", 2021
                    ))
                    .when()
                    .post("/api/v1/vehicles")
                    .then()
                    .statusCode(anyOf(is(201), is(400), is(409), is(422)));

            String orderId = given()
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "customerDocument", document,
                            "licensePlate", plate,
                            "description", "Direct complete check"
                    ))
                    .when()
                    .post("/api/v1/work-orders")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id");

            // Attempting to jump directly from RECEIVED to COMPLETED should be rejected
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("status", "COMPLETED", "reason", "Forcing direct completion"))
                    .when()
                    .patch("/api/v1/work-orders/{id}", orderId)
                    .then()
                    .statusCode(anyOf(is(400), is(422)));
        }
    }
}
