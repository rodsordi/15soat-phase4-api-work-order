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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    @DisplayName("createVehicle()")
    class CreateVehicleTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should successfully create vehicle and associate with customer")
            void shouldCreateVehicleSuccessfully() {
                // Arrange
                String document = "52998224725";
                String plate = "BRA2E19";
                Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", document);

                given(vehicleRepository.existsByLicensePlate(plate)).willReturn(false);
                given(customerRepository.findByDocument(document)).willReturn(Optional.of(customer));
                given(vehicleRepository.save(any(Vehicle.class))).willAnswer(inv -> inv.getArgument(0));

                // Act
                Vehicle result = vehicleService.createVehicle(document, plate, "Toyota", "Corolla", 2022);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getLicensePlate()).isEqualTo(plate);
                assertThat(result.getMake()).isEqualTo("Toyota");
                assertThat(result.getModel()).isEqualTo("Corolla");
                assertThat(result.getManufactureYear()).isEqualTo(2022);
                assertThat(result.getCustomer()).isSameAs(customer);
                assertThat(customer.getVehicles()).contains(result);
                verify(vehicleRepository).save(any(Vehicle.class));
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when vehicle license plate already exists")
            void shouldThrowExceptionWhenPlateAlreadyExists() {
                // Arrange
                String plate = "BRA2E19";
                given(vehicleRepository.existsByLicensePlate(plate)).willReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> vehicleService.createVehicle("52998224725", plate, "Toyota", "Corolla", 2022))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Vehicle already exists with license plate: " + plate);
                verify(customerRepository, never()).findByDocument(any());
            }

            @Test
            @DisplayName("Should throw DomainException when customer is not found")
            void shouldThrowExceptionWhenCustomerNotFound() {
                // Arrange
                String document = "52998224725";
                String plate = "BRA2E19";
                given(vehicleRepository.existsByLicensePlate(plate)).willReturn(false);
                given(customerRepository.findByDocument(document)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> vehicleService.createVehicle(document, plate, "Toyota", "Corolla", 2022))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Customer not found with document: " + document);
                verify(vehicleRepository, never()).save(any());
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
            @DisplayName("Should return vehicle when found by ID")
            void shouldReturnVehicleWhenFoundById() {
                // Arrange
                UUID id = UUID.randomUUID();
                Customer customer = Customer.create("John", "john@fiap.com.br", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);
                given(vehicleRepository.findById(id)).willReturn(Optional.of(vehicle));

                // Act
                Vehicle result = vehicleService.getById(id);

                // Assert
                assertThat(result).isSameAs(vehicle);
                verify(vehicleRepository).findById(id);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when vehicle is not found by ID")
            void shouldThrowExceptionWhenNotFoundById() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(vehicleRepository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> vehicleService.getById(id))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Vehicle not found with id: " + id);
            }
        }
    }

    @Nested
    @DisplayName("getByLicensePlate()")
    class GetByLicensePlateTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should return vehicle when found by license plate")
            void shouldReturnVehicleWhenFoundByPlate() {
                // Arrange
                String plate = "BRA2E19";
                Customer customer = Customer.create("John", "john@fiap.com.br", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, plate, "Toyota", "Corolla", 2022);
                given(vehicleRepository.findByLicensePlate(plate)).willReturn(Optional.of(vehicle));

                // Act
                Vehicle result = vehicleService.getByLicensePlate(plate);

                // Assert
                assertThat(result).isSameAs(vehicle);
                verify(vehicleRepository).findByLicensePlate(plate);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when vehicle is not found by license plate")
            void shouldThrowExceptionWhenNotFoundByPlate() {
                // Arrange
                String plate = "BRA2E19";
                given(vehicleRepository.findByLicensePlate(plate)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> vehicleService.getByLicensePlate(plate))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Vehicle not found with license plate: " + plate);
            }
        }
    }

    @Nested
    @DisplayName("updateVehicle()")
    class UpdateVehicleTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should update vehicle make, model and year successfully")
            void shouldUpdateVehicleDetails() {
                // Arrange
                UUID id = UUID.randomUUID();
                Customer customer = Customer.create("John", "john@fiap.com.br", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);
                given(vehicleRepository.findById(id)).willReturn(Optional.of(vehicle));
                given(vehicleRepository.save(vehicle)).willReturn(vehicle);

                // Act
                Vehicle result = vehicleService.updateVehicle(id, "Honda", "Civic", 2023);

                // Assert
                assertThat(result.getMake()).isEqualTo("Honda");
                assertThat(result.getModel()).isEqualTo("Civic");
                assertThat(result.getManufactureYear()).isEqualTo(2023);
                verify(vehicleRepository).save(vehicle);
            }

            @Test
            @DisplayName("Should preserve existing fields when update values are blank or null")
            void shouldPreserveExistingFieldsWhenNullOrBlank() {
                // Arrange
                UUID id = UUID.randomUUID();
                Customer customer = Customer.create("John", "john@fiap.com.br", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);
                given(vehicleRepository.findById(id)).willReturn(Optional.of(vehicle));
                given(vehicleRepository.save(vehicle)).willReturn(vehicle);

                // Act
                Vehicle result = vehicleService.updateVehicle(id, "", null, null);

                // Assert
                assertThat(result.getMake()).isEqualTo("Toyota");
                assertThat(result.getModel()).isEqualTo("Corolla");
                assertThat(result.getManufactureYear()).isEqualTo(2022);
                verify(vehicleRepository).save(vehicle);
            }
        }
    }

    @Nested
    @DisplayName("deleteVehicleById()")
    class DeleteVehicleTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should successfully delete vehicle when there are no active work orders")
            void shouldDeleteVehicleSuccessfully() {
                // Arrange
                UUID vehicleId = UUID.randomUUID();
                Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

                given(vehicleRepository.findById(vehicleId)).willReturn(Optional.of(vehicle));
                given(workOrderRepository.hasActiveWorkOrdersByVehicleId(vehicle.getId())).willReturn(false);

                // Act
                vehicleService.deleteVehicleById(vehicleId);

                // Assert
                verify(vehicleRepository).delete(vehicle);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw DomainException when vehicle has active work orders")
            void shouldThrowExceptionWhenVehicleHasActiveWorkOrders() {
                // Arrange
                UUID vehicleId = UUID.randomUUID();
                Customer customer = Customer.create("John Doe", "john@example.com", "52998224725");
                Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);

                given(vehicleRepository.findById(vehicleId)).willReturn(Optional.of(vehicle));
                given(workOrderRepository.hasActiveWorkOrdersByVehicleId(vehicle.getId())).willReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> vehicleService.deleteVehicleById(vehicleId))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Vehicle has active work orders in progress and cannot be deleted");
                verify(vehicleRepository, never()).delete(any());
            }
        }
    }
}
