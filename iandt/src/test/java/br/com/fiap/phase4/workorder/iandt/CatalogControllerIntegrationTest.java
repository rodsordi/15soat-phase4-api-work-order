package br.com.fiap.phase4.workorder.iandt;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Catalog Controller (Materials & Services) REST Integration Tests")
class CatalogControllerIntegrationTest extends WorkOrderIntegrationTest {

    @Nested
    @DisplayName("Material Management Scenarios")
    class MaterialManagementScenarios {

        @Test
        @DisplayName("Should create material, retrieve by ID, list, and update unit price via PATCH")
        void shouldCreateRetrieveAndPatchMaterial() {
            String sku = "PART-BRK-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            Map<String, Object> request = Map.of(
                    "sku", sku,
                    "name", "Front Brake Pad",
                    "description", "Ceramic front brake pad set",
                    "unitCost", 45.00,
                    "unitPrice", 95.00,
                    "initialStock", 20,
                    "minStock", 5
            );

            // 1. Create Material
            String materialId = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/materials")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("sku", equalTo(sku))
                    .body("unitPrice", is(95.0f))
                    .extract()
                    .path("id");

            // 2. Retrieve Material by ID
            given()
                    .when()
                    .get("/api/v1/materials/{id}", materialId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(materialId))
                    .body("sku", equalTo(sku));

            // 3. List materials
            given()
                    .queryParam("sku", sku)
                    .when()
                    .get("/api/v1/materials")
                    .then()
                    .statusCode(200)
                    .body("content", not(empty()));

            // 4. Update unit price via PATCH
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("unitPrice", 105.00))
                    .when()
                    .patch("/api/v1/materials/{id}", materialId)
                    .then()
                    .statusCode(200)
                    .body("unitPrice", is(105.0f));
        }

        @Test
        @DisplayName("Should reject material creation with duplicate SKU")
        void shouldRejectDuplicateSku() {
            String sku = "PART-DUP-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            Map<String, Object> request = Map.of(
                    "sku", sku,
                    "name", "Duplicate SKU Item",
                    "description", "Initial item",
                    "unitCost", 20.00,
                    "unitPrice", 40.00,
                    "initialStock", 10,
                    "minStock", 2
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/materials")
                    .then()
                    .statusCode(201);

            // Second creation with identical SKU should be rejected
            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/materials")
                    .then()
                    .statusCode(anyOf(is(400), is(409), is(422)));
        }
    }

    @Nested
    @DisplayName("Service Catalog Scenarios")
    class ServiceCatalogScenarios {

        @Test
        @DisplayName("Should create service in catalog, retrieve by ID, and list services")
        void shouldCreateAndRetrieveService() {
            String code = "SRV-OIL-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            Map<String, Object> request = Map.of(
                    "code", code,
                    "name", "Synthetic Oil Change Service",
                    "description", "Full oil change service including filter replacement",
                    "price", 150.00
            );

            String serviceId = given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/services")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("code", equalTo(code))
                    .body("price", is(150.0f))
                    .extract()
                    .path("id");

            given()
                    .when()
                    .get("/api/v1/services/{id}", serviceId)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(serviceId))
                    .body("code", equalTo(code));

            given()
                    .when()
                    .get("/api/v1/services")
                    .then()
                    .statusCode(200)
                    .body("content", not(empty()));
        }

        @Test
        @DisplayName("Should reject service creation with duplicate code")
        void shouldRejectDuplicateCode() {
            String code = "SRV-DUP-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            Map<String, Object> request = Map.of(
                    "code", code,
                    "name", "Duplicate Service Item",
                    "description", "First registered instance",
                    "price", 120.00
            );

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/services")
                    .then()
                    .statusCode(201);

            given()
                    .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/services")
                    .then()
                    .statusCode(anyOf(is(400), is(409), is(422)));
        }
    }
}
