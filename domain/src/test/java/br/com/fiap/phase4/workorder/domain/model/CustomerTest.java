package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Customer Domain Model Tests")
class CustomerTest {

    @Nested
    @DisplayName("Document (CPF / CNPJ) Validation Tests")
    class DocumentValidationTests {

        @Test
        @DisplayName("Should successfully create customer with valid 11-digit CPF")
        void shouldCreateCustomerWithValidCpf() {
            Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "52998224725");

            assertThat(customer).isNotNull();
            assertThat(customer.getDocument()).isEqualTo("52998224725");
            assertThat(customer.getName()).isEqualTo("Rodrigo Sordi");
            assertThat(customer.getEmail()).isEqualTo("rodrigo@fiap.com.br");
        }

        @Test
        @DisplayName("Should successfully create customer with valid 14-digit CNPJ")
        void shouldCreateCustomerWithValidCnpj() {
            Customer customer = Customer.create("Empresa ABC", "contato@abc.com", "00.000.000/0001-91");

            assertThat(customer).isNotNull();
            assertThat(customer.getDocument()).isEqualTo("00000000000191");
        }

        @Test
        @DisplayName("Should sanitize CPF formatting when creating customer")
        void shouldSanitizeCpfFormatting() {
            Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "529.982.247-25");

            assertThat(customer.getDocument()).isEqualTo("52998224725");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "11111111111", // Repeated digits
                "12345678900", // Invalid check digits
                "12345",       // Less than 11 digits
                "123456789012345", // More than 14 digits
                "abcdefghijk"  // Non-numeric
        })
        @DisplayName("Should throw ValidationException for invalid CPF documents")
        void shouldThrowExceptionForInvalidCpf(String invalidDoc) {
            assertThatThrownBy(() -> Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", invalidDoc))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Required Fields Validation Tests")
    class RequiredFieldsValidationTests {

        @Test
        @DisplayName("Should throw NullPointerException when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> new Customer(null, null, "rodrigo@fiap.com.br", "52998224725", null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Customer name is required");
        }

        @Test
        @DisplayName("Should throw NullPointerException when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            assertThatThrownBy(() -> new Customer(null, "Rodrigo Sordi", null, "52998224725", null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Customer email is required");
        }

        @Test
        @DisplayName("Should throw NullPointerException when document is null")
        void shouldThrowExceptionWhenDocumentIsNull() {
            assertThatThrownBy(() -> new Customer(null, "Rodrigo Sordi", "rodrigo@fiap.com.br", null, null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Customer document is required");
        }
    }

    @Nested
    @DisplayName("Business Methods Tests")
    class BusinessMethodsTests {

        @Test
        @DisplayName("Should update customer name and email")
        void shouldUpdateCustomer() {
            // Arrange
            Customer customer = Customer.create("Old Name", "old@fiap.com.br", "52998224725");

            // Act
            customer.update("New Name", "new@fiap.com.br");

            // Assert
            assertThat(customer.getName()).isEqualTo("New Name");
            assertThat(customer.getEmail()).isEqualTo("new@fiap.com.br");
        }

        @Test
        @DisplayName("Should ignore blank or null updates")
        void shouldIgnoreBlankOrNullUpdates() {
            // Arrange
            Customer customer = Customer.create("Original Name", "orig@fiap.com.br", "52998224725");

            // Act
            customer.update("   ", null);

            // Assert
            assertThat(customer.getName()).isEqualTo("Original Name");
            assertThat(customer.getEmail()).isEqualTo("orig@fiap.com.br");
        }

        @Test
        @DisplayName("Should associate vehicle and maintain bidirectional relationship")
        void shouldAddVehicle() {
            // Arrange
            Customer customer = Customer.create("Rodrigo", "rodrigo@fiap.com.br", "52998224725");
            Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

            // Act
            customer.addVehicle(vehicle);

            // Assert
            assertThat(customer.getVehicles()).contains(vehicle);
            assertThat(vehicle.getCustomer()).isSameAs(customer);
        }

        @Test
        @DisplayName("Should instantiate full constructor with custom timestamps and ID")
        void shouldInstantiateWithFullConstructor() {
            // Arrange
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            // Act
            Customer customer = new Customer(id, "Full Name", "full@fiap.com", "52998224725", now, now);

            // Assert
            assertThat(customer.getId()).isEqualTo(id);
            assertThat(customer.getCreatedAt()).isEqualTo(now);
            assertThat(customer.getUpdatedAt()).isEqualTo(now);
        }
    }
}
