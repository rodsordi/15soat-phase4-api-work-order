package br.com.fiap.phase4.workorder.domain;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.exception.ValidationException;
import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderEventPublisherPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import br.com.fiap.phase4.workorder.domain.usecase.CreateWorkOrderUseCaseImpl;
import br.com.fiap.phase4.workorder.domain.usecase.UpdateWorkOrderStatusUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Work Order Domain Tests")
class WorkOrderDomainTest {

    @Mock
    private WorkOrderRepositoryPort repository;

    @Mock
    private WorkOrderEventPublisherPort eventPublisher;

    @Mock
    private CustomerRepositoryPort customerRepository;

    @Mock
    private VehicleRepositoryPort vehicleRepository;

    @Mock
    private MaterialRepositoryPort materialRepository;

    private CreateWorkOrderUseCaseImpl createUseCase;
    private UpdateWorkOrderStatusUseCaseImpl updateUseCase;

    @BeforeEach
    void setUp() {
        createUseCase = new CreateWorkOrderUseCaseImpl(repository, customerRepository, vehicleRepository);
        updateUseCase = new UpdateWorkOrderStatusUseCaseImpl(repository, eventPublisher, materialRepository);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create Work Order with valid inputs")
        void shouldCreateWorkOrder() {
            when(repository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

            WorkOrder result = createUseCase.create("52998224725", "BRA2E19", "Brake service");

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.RECEIVED);
            assertThat(result.getCustomerDocument().formatted()).isEqualTo("529.982.247-25");
            assertThat(result.getLicensePlate().formatted()).isEqualTo("BRA2E19");
            verify(repository, times(1)).save(any(WorkOrder.class));
        }

        @Test
        @DisplayName("Should reject Work Order with invalid document")
        void shouldRejectInvalidDocument() {
            assertThatThrownBy(() -> createUseCase.create("00000000000", "BRA2E19", "Service"))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Status Update & Saga Event Publishing Tests")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should approve Work Order and publish WorkOrderApprovedEvent to Kafka")
        void shouldApproveAndPublishEvent() {
            UUID id = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            workOrder.finishDiagnosisAndSendBudget();
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            WorkOrder updated = updateUseCase.updateStatus(id, WorkOrderStatus.APPROVED, "Approved by customer", "trace-123");

            assertThat(updated.getStatus()).isEqualTo(WorkOrderStatus.APPROVED);
            verify(repository).save(workOrder);
            verify(eventPublisher).publishWorkOrderApproved(workOrder, "trace-123");
        }

        @Test
        @DisplayName("Should reject approval before budget is calculated")
        void shouldRejectApprovalBeforeBudgetCalculated() {
            UUID id = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            assertThatThrownBy(() -> updateUseCase.updateStatus(id, WorkOrderStatus.APPROVED, "Approved", "trace-123"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Cannot approve work order before budget is generated");
        }

        @Test
        @DisplayName("Should deliver completed Work Order successfully")
        void shouldDeliverCompletedWorkOrder() {
            UUID id = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            workOrder.finishDiagnosisAndSendBudget();
            workOrder.approve();
            workOrder.startExecution();
            workOrder.complete();
            workOrder.markAsPaid();
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            WorkOrder updated = updateUseCase.updateStatus(id, WorkOrderStatus.DELIVERED, "Vehicle delivered", "trace-123");

            assertThat(updated.getStatus()).isEqualTo(WorkOrderStatus.DELIVERED);
            assertThat(updated.getStatusHistory()).isNotEmpty();
            verify(repository).save(workOrder);
        }

        @Test
        @DisplayName("Should reject execution if budget was not approved")
        void shouldRejectExecutionWithoutApproval() {
            UUID id = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            workOrder.finishDiagnosisAndSendBudget();
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            assertThatThrownBy(() -> updateUseCase.updateStatus(id, WorkOrderStatus.EXECUTING, "Start repairs", "trace-123"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Cannot execute work order before budget is approved");
        }

        @Test
        @DisplayName("Should reject delivery if work order is completed but not paid")
        void shouldRejectDeliveryBeforePayment() {
            UUID id = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            workOrder.finishDiagnosisAndSendBudget();
            workOrder.approve();
            workOrder.startExecution();
            workOrder.complete();
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            assertThatThrownBy(() -> updateUseCase.updateStatus(id, WorkOrderStatus.DELIVERED, "Deliver", "trace-123"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Work order cannot be delivered before payment is confirmed");
        }

        @Test
        @DisplayName("Should reject cancellation of already paid or delivered work order")
        void shouldRejectCancellationOfFinalizedWorkOrder() {
            UUID id = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            workOrder.finishDiagnosisAndSendBudget();
            workOrder.approve();
            workOrder.startExecution();
            workOrder.complete();
            workOrder.markAsPaid();
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            assertThatThrownBy(() -> updateUseCase.updateStatus(id, WorkOrderStatus.CANCELED, "Client requested cancellation", "trace-123"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Cannot cancel an already finalized work order");
        }

        @Test
        @DisplayName("Should reserve material on approval and release on cancellation")
        void shouldReserveOnApproveAndReleaseOnCancel() {
            UUID id = UUID.randomUUID();
            UUID materialId = UUID.randomUUID();
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            workOrder.addMaterial(materialId, "Brake Pad", "PART-01", Money.of(new BigDecimal("50.00")), 2);
            workOrder.finishDiagnosisAndSendBudget();

            br.com.fiap.phase4.workorder.domain.model.Material material = br.com.fiap.phase4.workorder.domain.model.Material.create(
                    "PART-01", "Brake Pad", "Desc",
                    Money.of(new BigDecimal("20.00")),
                    Money.of(new BigDecimal("50.00")),
                    10, 2
            );

            when(repository.findById(id)).thenReturn(Optional.of(workOrder));
            when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));

            // Approve: should reserve 2 units
            updateUseCase.updateStatus(id, WorkOrderStatus.APPROVED, "Approved", "trace-1");
            assertThat(material.getReservedQuantity()).isEqualTo(2);

            // Cancel: should release 2 units
            updateUseCase.updateStatus(id, WorkOrderStatus.CANCELED, "Client canceled", "trace-2");
            assertThat(material.getReservedQuantity()).isZero();
        }
    }

    @Nested
    @DisplayName("WorkOrder Entity Domain Methods Tests")
    class WorkOrderEntityDomainMethodsTests {

        @Test
        @DisplayName("Should add services and calculate total amount correctly")
        void shouldAddServicesAndCalculateTotal() {
            // Arrange
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            UUID serviceId1 = UUID.randomUUID();
            UUID serviceId2 = UUID.randomUUID();

            // Act
            workOrder.addService(serviceId1, "Oil Change", "SRV-01", Money.of(new BigDecimal("100.00")), 1);
            workOrder.addService(serviceId2, Money.of(new BigDecimal("50.00")), 2);

            // Assert
            assertThat(workOrder.getServices()).hasSize(2);
            assertThat(workOrder.getTotalAmount()).isEqualTo(Money.of(new BigDecimal("200.00")));
        }

        @Test
        @DisplayName("Should add materials and calculate total amount correctly")
        void shouldAddMaterialsAndCalculateTotal() {
            // Arrange
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            UUID matId1 = UUID.randomUUID();
            UUID matId2 = UUID.randomUUID();

            // Act
            workOrder.addMaterial(matId1, "Filter", "MAT-01", Money.of(new BigDecimal("30.00")), 1);
            workOrder.addMaterial(matId2, Money.of(new BigDecimal("15.00")), 2);

            // Assert
            assertThat(workOrder.getMaterials()).hasSize(2);
            assertThat(workOrder.getTotalAmount()).isEqualTo(Money.of(new BigDecimal("60.00")));
        }

        @Test
        @DisplayName("Should handle setTotalAmount, setCustomer and setVehicle")
        void shouldHandleSetters() {
            // Arrange
            Customer customer = Customer.create("Rodrigo Sordi", "rodrigo@fiap.com.br", "52998224725");
            Vehicle vehicle = Vehicle.create(customer, "BRA2E19", "Toyota", "Corolla", 2022);
            WorkOrder workOrder = WorkOrder.create(customer, vehicle, "Full Service");

            // Act
            workOrder.setCustomer(customer);
            workOrder.setVehicle(vehicle);
            workOrder.setTotalAmount(Money.of(new BigDecimal("350.00")));

            // Assert
            assertThat(workOrder.getCustomer()).isSameAs(customer);
            assertThat(workOrder.getVehicle()).isSameAs(vehicle);
            assertThat(workOrder.getCustomerDocument().value()).isEqualTo("52998224725");
            assertThat(workOrder.getLicensePlate().value()).isEqualTo("BRA2E19");
            assertThat(workOrder.getTotalAmount()).isEqualTo(Money.of(new BigDecimal("350.00")));
        }

        @Test
        @DisplayName("Should return Duration.ZERO or positive duration from calculateCurrentStatusDuration()")
        void shouldCalculateStatusDuration() {
            // Arrange
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Test"
            );

            // Act
            Duration duration = workOrder.calculateCurrentStatusDuration();

            // Assert
            assertThat(duration).isNotNull();
            assertThat(duration.isNegative()).isFalse();
        }

        @Test
        @DisplayName("Should not duplicate history when transitionTo same status")
        void shouldNotDuplicateHistoryWhenSameStatus() {
            // Arrange
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Test"
            );
            int initialHistorySize = workOrder.getStatusHistory().size();

            // Act
            workOrder.transitionTo(WorkOrderStatus.RECEIVED, "Same status", null);

            // Assert
            assertThat(workOrder.getStatusHistory()).hasSize(initialHistorySize);
        }

        @Test
        @DisplayName("Should transition to PAYMENT_REJECTED via rejectPayment()")
        void shouldRejectPayment() {
            // Arrange
            WorkOrder workOrder = WorkOrder.create(
                    new CpfOrCnpj("52998224725"),
                    new LicensePlate("BRA2E19"),
                    "Test"
            );

            // Act
            workOrder.rejectPayment("Insufficient funds");

            // Assert
            assertThat(workOrder.getStatus()).isEqualTo(WorkOrderStatus.PAYMENT_REJECTED);
        }

        @Test
        @DisplayName("Should create WorkOrder with 12-parameter constructor")
        void shouldCreateWith12ParamConstructor() {
            // Arrange
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            // Act
            WorkOrder wo = new WorkOrder(
                    id, null, null, "52998224725", "BRA2E19",
                    "Custom description", WorkOrderStatus.RECEIVED,
                    new BigDecimal("100.00"), List.of(), List.of(),
                    now, now
            );

            // Assert
            assertThat(wo.getId()).isEqualTo(id);
            assertThat(wo.getDescription()).isEqualTo("Custom description");
            assertThat(wo.getCreatedAt()).isEqualTo(now);
            assertThat(wo.getUpdatedAt()).isEqualTo(now);
        }
    }
}
