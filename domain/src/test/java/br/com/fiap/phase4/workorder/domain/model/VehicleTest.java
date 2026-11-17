package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vehicle Domain Model Tests")
class VehicleTest {

    private Customer createCustomer() {
        return Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "52998224725");
    }

    @Nested
    @DisplayName("create() and Constructor Tests")
    class CreationTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @ParameterizedTest
            @CsvSource({
                    "BRA2E19, Toyota, Corolla, 2022",
                    "ABC1234, Honda, Civic, 2020",
                    "XYZ9A87, Volkswagen, Golf, 2023"
            })
            @DisplayName("Should create vehicle with valid parameters")
            void shouldCreateVehicleWithValidParams(String plate, String make, String model, int year) {
                // Arrange
                Customer customer = createCustomer();

                // Act
                Vehicle vehicle = Vehicle.create(customer, plate, make, model, year);

                // Assert
                assertThat(vehicle).isNotNull();
                assertThat(vehicle.getId()).isNotNull();
                assertThat(vehicle.getCustomer()).isSameAs(customer);
                assertThat(vehicle.getLicensePlate()).isEqualTo(plate);
                assertThat(vehicle.getMake()).isEqualTo(make);
                assertThat(vehicle.getModel()).isEqualTo(model);
                assertThat(vehicle.getManufactureYear()).isEqualTo(year);
                assertThat(vehicle.getCreatedAt()).isNotNull();
                assertThat(vehicle.getUpdatedAt()).isNotNull();
            }

            @Test
            @DisplayName("Should create vehicle with full constructor")
            void shouldCreateWithFullConstructor() {
                // Arrange
                Customer customer = createCustomer();
                UUID id = UUID.randomUUID();
                Instant now = Instant.now();

                // Act
                Vehicle vehicle = new Vehicle(id, customer, "BRA2E19", "Toyota", "Corolla", 2022, now, now);

                // Assert
                assertThat(vehicle.getId()).isEqualTo(id);
                assertThat(vehicle.getCreatedAt()).isEqualTo(now);
                assertThat(vehicle.getUpdatedAt()).isEqualTo(now);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw NullPointerException when customer is null")
            void shouldThrowWhenCustomerIsNull() {
                assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), null, "BRA2E19", "Toyota", "Corolla", 2022, null, null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessageContaining("Customer is required");
            }

            @Test
            @DisplayName("Should throw NullPointerException when make is null")
            void shouldThrowWhenMakeIsNull() {
                Customer customer = createCustomer();
                assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), customer, "BRA2E19", null, "Corolla", 2022, null, null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessageContaining("Vehicle make is required");
            }

            @Test
            @DisplayName("Should throw NullPointerException when model is null")
            void shouldThrowWhenModelIsNull() {
                Customer customer = createCustomer();
                assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), customer, "BRA2E19", "Toyota", null, 2022, null, null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessageContaining("Vehicle model is required");
            }

            @Test
            @DisplayName("Should throw NullPointerException when manufactureYear is null")
            void shouldThrowWhenYearIsNull() {
                Customer customer = createCustomer();
                assertThatThrownBy(() -> new Vehicle(UUID.randomUUID(), customer, "BRA2E19", "Toyota", "Corolla", null, null, null))
                        .isInstanceOf(NullPointerException.class)
                        .hasMessageContaining("Manufacture year is required");
            }

            @ParameterizedTest
            @ValueSource(strings = {"INVALID", "12345", "A", "ABCDEFG"})
            @DisplayName("Should throw ValidationException when license plate is invalid")
            void shouldThrowWhenLicensePlateIsInvalid(String invalidPlate) {
                Customer customer = createCustomer();
                assertThatThrownBy(() -> Vehicle.create(customer, invalidPlate, "Toyota", "Corolla", 2022))
                        .isInstanceOf(ValidationException.class);
            }
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should update vehicle make, model and year")
            void shouldUpdateAllFields() {
                // Arrange
                Customer customer = createCustomer();
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

                // Act
                vehicle.update("Honda", "Civic", 2024);

                // Assert
                assertThat(vehicle.getMake()).isEqualTo("Honda");
                assertThat(vehicle.getModel()).isEqualTo("Civic");
                assertThat(vehicle.getManufactureYear()).isEqualTo(2024);
            }

            @Test
            @DisplayName("Should ignore blank or null parameters")
            void shouldIgnoreBlankOrNull() {
                // Arrange
                Customer customer = createCustomer();
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

                // Act
                vehicle.update("", null, null);

                // Assert
                assertThat(vehicle.getMake()).isEqualTo("Toyota");
                assertThat(vehicle.getModel()).isEqualTo("Corolla");
                assertThat(vehicle.getManufactureYear()).isEqualTo(2022);
            }
        }
    }
}
