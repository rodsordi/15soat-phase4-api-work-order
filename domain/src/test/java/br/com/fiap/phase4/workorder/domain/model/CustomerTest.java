package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
}
