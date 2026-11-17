package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private WorkOrderRepositoryPort workOrderRepository;

    @InjectMocks
    private CustomerService customerService;

    @Nested
    @DisplayName("createCustomer()")
    class CreateCustomerTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should successfully create and save customer when document is unique")
            void shouldCreateCustomerWhenDocumentIsUnique() {
                // Arrange
                String name = "Rodrigo Sordi";
                String email = "rodrigo@fiap.com.br";
                String document = "52998224725";

                given(customerRepository.existsByDocument(document)).willReturn(false);
                given(customerRepository.save(any(Customer.class))).willAnswer(inv -> inv.getArgument(0));

                // Act
                Customer result = customerService.createCustomer(name, email, document);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo(name);
                assertThat(result.getEmail()).isEqualTo(email);
                assertThat(result.getDocument()).isEqualTo(document);
                verify(customerRepository).save(any(Customer.class));
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when customer with document already exists")
            void shouldThrowExceptionWhenDocumentAlreadyExists() {
                // Arrange
                String document = "52998224725";
                given(customerRepository.existsByDocument(document)).willReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> customerService.createCustomer("John", "john@fiap.com.br", document))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Customer already exists with document: " + document);
                verify(customerRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should return customer when found by ID")
            void shouldReturnCustomerWhenFoundById() {
                // Arrange
                UUID id = UUID.randomUUID();
                Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "52998224725");
                given(customerRepository.findById(id)).willReturn(Optional.of(customer));

                // Act
                Customer result = customerService.getById(id);

                // Assert
                assertThat(result).isSameAs(customer);
                verify(customerRepository).findById(id);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when customer not found by ID")
            void shouldThrowExceptionWhenNotFoundById() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(customerRepository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> customerService.getById(id))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Customer not found with id: " + id);
            }
        }
    }

    @Nested
    @DisplayName("getByDocument()")
    class GetByDocumentTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should return customer when found by document")
            void shouldReturnCustomerWhenFoundByDocument() {
                // Arrange
                String document = "52998224725";
                Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", document);
                given(customerRepository.findByDocument(document)).willReturn(Optional.of(customer));

                // Act
                Customer result = customerService.getByDocument(document);

                // Assert
                assertThat(result).isSameAs(customer);
                verify(customerRepository).findByDocument(document);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when customer not found by document")
            void shouldThrowExceptionWhenNotFoundByDocument() {
                // Arrange
                String document = "52998224725";
                given(customerRepository.findByDocument(document)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> customerService.getByDocument(document))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Customer not found with document: " + document);
            }
        }
    }

    @Nested
    @DisplayName("updateCustomer()")
    class UpdateCustomerTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should update customer name and email successfully")
            void shouldUpdateCustomerNameAndEmail() {
                // Arrange
                UUID id = UUID.randomUUID();
                Customer customer = Customer.create("Old Name", "old@fiap.com.br", "52998224725");
                given(customerRepository.findById(id)).willReturn(Optional.of(customer));
                given(customerRepository.save(customer)).willReturn(customer);

                // Act
                Customer result = customerService.updateCustomer(id, "New Name", "new@fiap.com.br");

                // Assert
                assertThat(result.getName()).isEqualTo("New Name");
                assertThat(result.getEmail()).isEqualTo("new@fiap.com.br");
                verify(customerRepository).save(customer);
            }

            @Test
            @DisplayName("Should keep existing fields when update values are blank or null")
            void shouldKeepExistingFieldsWhenValuesAreNullOrBlank() {
                // Arrange
                UUID id = UUID.randomUUID();
                Customer customer = Customer.create("Original Name", "original@fiap.com.br", "52998224725");
                given(customerRepository.findById(id)).willReturn(Optional.of(customer));
                given(customerRepository.save(customer)).willReturn(customer);

                // Act
                Customer result = customerService.updateCustomer(id, "", null);

                // Assert
                assertThat(result.getName()).isEqualTo("Original Name");
                assertThat(result.getEmail()).isEqualTo("original@fiap.com.br");
                verify(customerRepository).save(customer);
            }
        }
    }

    @Nested
    @DisplayName("deleteCustomerById()")
    class DeleteCustomerTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should successfully delete customer when there are no vehicles and no active work orders")
            void shouldDeleteCustomerSuccessfully() {
                // Arrange
                UUID customerId = UUID.randomUUID();
                Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");

                given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
                given(workOrderRepository.hasActiveWorkOrdersByCustomerId(customer.getId())).willReturn(false);

                // Act
                customerService.deleteCustomerById(customerId);

                // Assert
                verify(customerRepository).delete(customer);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when customer has associated vehicles")
            void shouldThrowExceptionWhenCustomerHasVehicles() {
                // Arrange
                UUID customerId = UUID.randomUUID();
                Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);
                customer.addVehicle(vehicle);

                given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

                // Act & Assert
                assertThatThrownBy(() -> customerService.deleteCustomerById(customerId))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Customer has associated vehicles and cannot be deleted");
                verify(customerRepository, never()).delete(any());
            }

            @Test
            @DisplayName("Should throw DomainException when customer has active work orders")
            void shouldThrowExceptionWhenCustomerHasActiveWorkOrders() {
                // Arrange
                UUID customerId = UUID.randomUUID();
                Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");

                given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
                given(workOrderRepository.hasActiveWorkOrdersByCustomerId(customer.getId())).willReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> customerService.deleteCustomerById(customerId))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Customer has active work orders in progress");
                verify(customerRepository, never()).delete(any());
            }
        }
    }
}
