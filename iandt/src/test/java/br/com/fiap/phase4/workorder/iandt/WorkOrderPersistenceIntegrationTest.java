package br.com.fiap.phase4.workorder.iandt;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Work Order Relational Persistence Integration Tests with PostgreSQL Testcontainers")
class WorkOrderPersistenceIntegrationTest extends WorkOrderIntegrationTest {

    @Test
    @DisplayName("Should persist Customer and Vehicle with bidirectional relationship and foreign keys in PostgreSQL")
    void shouldPersistCustomerAndVehicle() {
        Customer customer = Customer.create("Rodrigo Sordi", "rodrigo.sordi@fiap.com.br", "52998224725");
        Customer savedCustomer = customerRepository.saveAndFlush(customer);

        Vehicle vehicle = Vehicle.create(savedCustomer, "BRA2E19", "Toyota", "Corolla Cross", 2024);
        Vehicle savedVehicle = vehicleRepository.saveAndFlush(vehicle);

        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedVehicle.getId()).isNotNull();

        Optional<Customer> retrievedCustomer = customerRepository.findByDocument("52998224725");
        assertThat(retrievedCustomer).isPresent();
        assertThat(retrievedCustomer.get().getName()).isEqualTo("Rodrigo Sordi");

        Optional<Vehicle> retrievedVehicle = vehicleRepository.findByLicensePlate("BRA2E19");
        assertThat(retrievedVehicle).isPresent();
        assertThat(retrievedVehicle.get().getCustomer().getId()).isEqualTo(savedCustomer.getId());
    }

    @Test
    @DisplayName("Should persist ServiceCatalog and Material items in PostgreSQL database")
    void shouldPersistCatalogAndMaterial() {
        ServiceCatalog service = ServiceCatalog.create("SRV-001", "Brake Pad Replacement", "Standard replacement", new Money(new BigDecimal("180.00")), 60L);
        ServiceCatalog savedService = serviceCatalogRepository.saveAndFlush(service);

        Material material = Material.create("MAT-OIL-01", "Synthetic Motor Oil 5W30", "Engine Oil 1L", new Money(new BigDecimal("25.00")), new Money(new BigDecimal("45.00")), 100, 10);
        Material savedMaterial = materialRepository.saveAndFlush(material);

        assertThat(savedService.getId()).isNotNull();
        assertThat(savedMaterial.getId()).isNotNull();

        assertThat(serviceCatalogRepository.findById(savedService.getId())).isPresent();
        assertThat(materialRepository.findById(savedMaterial.getId())).isPresent();
    }

    @Test
    @Transactional
    @DisplayName("Should persist WorkOrder with aggregate status history and items in PostgreSQL")
    void shouldPersistWorkOrderWithStatusHistory() {
        Customer customer = customerRepository.saveAndFlush(Customer.create("Carlos Silva", "carlos@test.com", "66202667036"));
        Vehicle vehicle = vehicleRepository.saveAndFlush(Vehicle.create(customer, "ABC1D23", "Honda", "Civic", 2022));

        WorkOrder workOrder = WorkOrder.create(customer, vehicle, "Oil leak diagnosis and general checkup");
        workOrder.startDiagnosis();
        WorkOrder savedWorkOrder = workOrderRepository.saveAndFlush(workOrder);

        assertThat(savedWorkOrder.getId()).isNotNull();
        assertThat(savedWorkOrder.getStatus()).isEqualTo(WorkOrderStatus.DIAGNOSING);
        assertThat(savedWorkOrder.getCustomerDocument().value()).isEqualTo("66202667036");
        assertThat(savedWorkOrder.getLicensePlate().value()).isEqualTo("ABC1D23");

        Optional<WorkOrder> foundOrder = workOrderRepository.findById(savedWorkOrder.getId());
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getStatusHistory()).isNotEmpty();
    }
}
