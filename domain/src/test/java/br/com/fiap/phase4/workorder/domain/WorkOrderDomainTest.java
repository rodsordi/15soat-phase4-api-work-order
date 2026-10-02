package br.com.fiap.phase4.workorder.domain;

import br.com.fiap.phase4.commons.domain.exception.ValidationException;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
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

    private CreateWorkOrderUseCaseImpl createUseCase;
    private UpdateWorkOrderStatusUseCaseImpl updateUseCase;

    @BeforeEach
    void setUp() {
        createUseCase = new CreateWorkOrderUseCaseImpl(repository, customerRepository, vehicleRepository);
        updateUseCase = new UpdateWorkOrderStatusUseCaseImpl(repository, eventPublisher);
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
                    new br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj("52998224725"),
                    new br.com.fiap.phase4.commons.domain.vo.LicensePlate("BRA2E19"),
                    "Maintenance"
            );
            when(repository.findById(id)).thenReturn(Optional.of(workOrder));

            WorkOrder updated = updateUseCase.updateStatus(id, WorkOrderStatus.APPROVED, "Approved by customer", "trace-123");

            assertThat(updated.getStatus()).isEqualTo(WorkOrderStatus.APPROVED);
            verify(repository).save(workOrder);
            verify(eventPublisher).publishWorkOrderApproved(workOrder, "trace-123");
        }
    }
}
