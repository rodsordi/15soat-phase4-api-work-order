package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepositoryPort vehicleRepository;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private WorkOrderRepositoryPort workOrderRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Nested
    @DisplayName("Delete Vehicle Tests")
    class DeleteVehicleTests {

        @Test
        @DisplayName("Should successfully delete vehicle when there are no active work orders")
        void shouldDeleteVehicleSuccessfully() {
            UUID vehicleId = UUID.randomUUID();
            Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");
            Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

            when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
            when(workOrderRepository.hasActiveWorkOrdersByVehicleId(vehicle.getId())).thenReturn(false);

            vehicleService.deleteVehicleById(vehicleId);

            verify(vehicleRepository).delete(vehicle);
        }

        @Test
        @DisplayName("Should throw DomainException when vehicle has active work orders")
        void shouldThrowExceptionWhenVehicleHasActiveWorkOrders() {
            UUID vehicleId = UUID.randomUUID();
            Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");
            Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

            when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
            when(workOrderRepository.hasActiveWorkOrdersByVehicleId(vehicle.getId())).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.deleteVehicleById(vehicleId))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Vehicle has active work orders in progress");
        }
    }
}
