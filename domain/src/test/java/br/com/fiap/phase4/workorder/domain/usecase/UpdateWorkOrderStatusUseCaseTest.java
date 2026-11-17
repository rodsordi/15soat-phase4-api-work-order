package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderEventPublisherPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderStatusMetricsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateWorkOrderStatusUseCase Unit Tests")
class UpdateWorkOrderStatusUseCaseTest {

    @Mock
    private WorkOrderRepositoryPort repository;

    @Mock
    private WorkOrderEventPublisherPort eventPublisher;

    @Mock
    private MaterialRepositoryPort materialRepository;

    @Mock
    private WorkOrderStatusMetricsPort metricsPort;

    private UpdateWorkOrderStatusUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateWorkOrderStatusUseCaseImpl(repository, eventPublisher, materialRepository, metricsPort);
    }

    private WorkOrder createTestWorkOrder(WorkOrderStatus initialStatus) {
        WorkOrder workOrder = WorkOrder.create(
                new CpfOrCnpj("52998224725"),
                new LicensePlate("BRA2E19"),
                "Periodic maintenance"
        );
        workOrder.setStatus(initialStatus);
        return workOrder;
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatusTests {

        @Nested
        @DisplayName("State Transitions - Success Scenarios")
        class StateTransitionsSuccess {

            @Test
            @DisplayName("Should transition from RECEIVED to DIAGNOSING")
            void shouldTransitionToDiagnosing() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.RECEIVED);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.DIAGNOSING, null, "trace-1");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.DIAGNOSING);
                verify(repository).save(workOrder);
                verify(metricsPort).recordStatusDuration(eq(WorkOrderStatus.RECEIVED), any(Duration.class));
            }

            @Test
            @DisplayName("Should transition from DIAGNOSING to WAITING_APPROVAL and publish estimate requested event")
            void shouldTransitionToWaitingApproval() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.DIAGNOSING);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.WAITING_APPROVAL, null, "trace-2");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.WAITING_APPROVAL);
                verify(repository).save(workOrder);
                verify(eventPublisher).publishWorkOrderEstimateRequested(workOrder, "trace-2");
            }

            @Test
            @DisplayName("Should transition from WAITING_APPROVAL to APPROVED, reserve materials and publish approved event")
            void shouldTransitionToApprovedAndReserveMaterials() {
                // Arrange
                UUID id = UUID.randomUUID();
                UUID materialId = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.WAITING_APPROVAL);
                workOrder.addMaterial(materialId, Money.of(new BigDecimal("50.00")), 2);

                Material material = Material.create("MAT-OIL-01", "Synthetic Oil 5W30", "Engine oil",
                        Money.of(new BigDecimal("20.00")), Money.of(new BigDecimal("50.00")), 10, 2);

                given(repository.findById(id)).willReturn(Optional.of(workOrder));
                given(materialRepository.findById(materialId)).willReturn(Optional.of(material));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.APPROVED, null, "trace-3");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.APPROVED);
                verify(materialRepository).save(material);
                assertThat(material.getReservedQuantity()).isEqualTo(2);
                verify(repository).save(workOrder);
                verify(eventPublisher).publishWorkOrderApproved(workOrder, "trace-3");
            }

            @Test
            @DisplayName("Should transition from APPROVED to EXECUTING")
            void shouldTransitionToExecuting() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.APPROVED);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.EXECUTING, null, "trace-4");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.EXECUTING);
                verify(repository).save(workOrder);
            }

            @Test
            @DisplayName("Should transition from EXECUTING to COMPLETED, conclude reserved materials and publish completed event")
            void shouldTransitionToCompletedAndConcludeMaterials() {
                // Arrange
                UUID id = UUID.randomUUID();
                UUID materialId = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.EXECUTING);
                workOrder.addMaterial(materialId, Money.of(new BigDecimal("50.00")), 3);

                Material material = Material.create("MAT-OIL-01", "Synthetic Oil", "Oil",
                        Money.of(new BigDecimal("20.00")), Money.of(new BigDecimal("50.00")), 10, 2);
                material.reserveQuantity(3);

                given(repository.findById(id)).willReturn(Optional.of(workOrder));
                given(materialRepository.findById(materialId)).willReturn(Optional.of(material));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.COMPLETED, null, "trace-5");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
                verify(materialRepository).save(material);
                assertThat(material.getReservedQuantity()).isZero();
                assertThat(material.getStockQuantity()).isEqualTo(7);
                verify(repository).save(workOrder);
                verify(eventPublisher).publishWorkOrderCompleted(workOrder, "trace-5");
            }

            @Test
            @DisplayName("Should transition from COMPLETED to PAID")
            void shouldTransitionToPaid() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.COMPLETED);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.PAID, null, "trace-6");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.PAID);
                verify(repository).save(workOrder);
            }

            @Test
            @DisplayName("Should transition from PAID to DELIVERED")
            void shouldTransitionToDelivered() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.PAID);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.DELIVERED, null, "trace-7");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.DELIVERED);
                verify(repository).save(workOrder);
            }

            @Test
            @DisplayName("Should cancel work order and release reserved materials when previous status was APPROVED")
            void shouldCancelAndReleaseMaterialsWhenApproved() {
                // Arrange
                UUID id = UUID.randomUUID();
                UUID materialId = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.APPROVED);
                workOrder.addMaterial(materialId, Money.of(new BigDecimal("50.00")), 2);

                Material material = Material.create("MAT-OIL-01", "Oil", "Oil",
                        Money.of(new BigDecimal("20.00")), Money.of(new BigDecimal("50.00")), 10, 2);
                material.reserveQuantity(2);

                given(repository.findById(id)).willReturn(Optional.of(workOrder));
                given(materialRepository.findById(materialId)).willReturn(Optional.of(material));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.CANCELED, "Customer gave up", "trace-8");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.CANCELED);
                verify(materialRepository).save(material);
                assertThat(material.getReservedQuantity()).isZero();
                verify(repository).save(workOrder);
            }

            @Test
            @DisplayName("Should reject payment and release reserved materials when previous status was APPROVED")
            void shouldRejectPaymentAndReleaseMaterials() {
                // Arrange
                UUID id = UUID.randomUUID();
                UUID materialId = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.APPROVED);
                workOrder.addMaterial(materialId, Money.of(new BigDecimal("50.00")), 1);

                Material material = Material.create("MAT-FLT-01", "Filter", "Filter",
                        Money.of(new BigDecimal("10.00")), Money.of(new BigDecimal("50.00")), 5, 1);
                material.reserveQuantity(1);

                given(repository.findById(id)).willReturn(Optional.of(workOrder));
                given(materialRepository.findById(materialId)).willReturn(Optional.of(material));

                // Act
                WorkOrder result = useCase.updateStatus(id, WorkOrderStatus.PAYMENT_REJECTED, "Card declined", "trace-9");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.PAYMENT_REJECTED);
                verify(materialRepository).save(material);
                assertThat(material.getReservedQuantity()).isZero();
                verify(repository).save(workOrder);
            }
        }

        @Nested
        @DisplayName("Validation and Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw ResourceNotFoundException when work order does not exist")
            void shouldThrowExceptionWhenNotFound() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(repository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> useCase.updateStatus(id, WorkOrderStatus.DIAGNOSING, null, "trace"))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining(id.toString());
                verify(repository, never()).save(any());
            }

            @Test
            @DisplayName("Should throw DomainException when transitioning to DIAGNOSING from invalid status")
            void shouldThrowExceptionWhenTransitioningFromInvalidStatus() {
                // Arrange
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.COMPLETED);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act & Assert
                assertThatThrownBy(() -> useCase.updateStatus(id, WorkOrderStatus.DIAGNOSING, null, "trace"))
                        .isInstanceOf(DomainException.class)
                        .hasMessageContaining("Cannot start diagnosis for a work order in status: COMPLETED");
            }

            @Test
            @DisplayName("Should handle 3-arg constructor gracefully without metrics port")
            void shouldWorkWithoutMetricsPort() {
                // Arrange
                UpdateWorkOrderStatusUseCaseImpl simpleUseCase = new UpdateWorkOrderStatusUseCaseImpl(
                        repository, eventPublisher, materialRepository
                );
                UUID id = UUID.randomUUID();
                WorkOrder workOrder = createTestWorkOrder(WorkOrderStatus.RECEIVED);
                given(repository.findById(id)).willReturn(Optional.of(workOrder));

                // Act
                WorkOrder result = simpleUseCase.updateStatus(id, WorkOrderStatus.DIAGNOSING, null, "trace-10");

                // Assert
                assertThat(result.getStatus()).isEqualTo(WorkOrderStatus.DIAGNOSING);
                verify(repository).save(workOrder);
            }
        }
    }
}
