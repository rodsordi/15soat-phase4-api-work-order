package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkOrderEventPublisherPort Unit Tests")
class WorkOrderEventPublisherPortTest {

    @Test
    @DisplayName("Should delegate publishWorkOrderBudgetRequested() to publishWorkOrderEstimateRequested()")
    void shouldDelegateBudgetRequestedToEstimateRequested() {
        // Arrange
        AtomicBoolean called = new AtomicBoolean(false);
        WorkOrder workOrder = WorkOrder.create(
                new CpfOrCnpj("52998224725"),
                new LicensePlate("BRA2E19"),
                "Inspection"
        );

        WorkOrderEventPublisherPort publisher = new WorkOrderEventPublisherPort() {
            @Override
            public void publishWorkOrderApproved(WorkOrder wo, String traceId) {}

            @Override
            public void publishWorkOrderCompleted(WorkOrder wo, String traceId) {}

            @Override
            public void publishWorkOrderEstimateRequested(WorkOrder wo, String traceId) {
                called.set(true);
            }
        };

        // Act
        publisher.publishWorkOrderBudgetRequested(workOrder, "trace-123");

        // Assert
        assertThat(called.get()).isTrue();
    }
}
