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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @DisplayName("Delete Customer Tests")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should successfully delete customer when there are no vehicles and no active work orders")
        void shouldDeleteCustomerSuccessfully() {
            UUID customerId = UUID.randomUUID();
            Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(workOrderRepository.hasActiveWorkOrdersByCustomerId(customer.getId())).thenReturn(false);

            customerService.deleteCustomerById(customerId);

            verify(customerRepository).delete(customer);
        }

        @Test
        @DisplayName("Should throw DomainException when customer has associated vehicles")
        void shouldThrowExceptionWhenCustomerHasVehicles() {
            UUID customerId = UUID.randomUUID();
            Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");
            Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);
            customer.addVehicle(vehicle);

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

            assertThatThrownBy(() -> customerService.deleteCustomerById(customerId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Customer has associated vehicles");
        }

        @Test
        @DisplayName("Should throw DomainException when customer has active work orders")
        void shouldThrowExceptionWhenCustomerHasActiveWorkOrders() {
            UUID customerId = UUID.randomUUID();
            Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");

            when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
            when(workOrderRepository.hasActiveWorkOrdersByCustomerId(customer.getId())).thenReturn(true);

            assertThatThrownBy(() -> customerService.deleteCustomerById(customerId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Customer has active work orders in progress");
        }
    }
}
