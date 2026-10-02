package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateWorkOrderStatusUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderEventPublisherPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateWorkOrderStatusUseCaseImpl implements UpdateWorkOrderStatusUseCase {

    private final WorkOrderRepositoryPort repository;
    private final WorkOrderEventPublisherPort eventPublisher;

    @Override
    public WorkOrder updateStatus(UUID workOrderId, WorkOrderStatus newStatus, String reason, String traceId) {
        WorkOrder workOrder = repository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkOrder", workOrderId));

        switch (newStatus) {
            case APPROVED -> {
                workOrder.approve();
                repository.save(workOrder);
                eventPublisher.publishWorkOrderApproved(workOrder, traceId);
            }
            case CANCELED -> {
                workOrder.cancel(reason);
                repository.save(workOrder);
            }
            case PAYMENT_REJECTED -> {
                workOrder.rejectPayment(reason);
                repository.save(workOrder);
            }
            case COMPLETED -> {
                workOrder.complete();
                repository.save(workOrder);
                eventPublisher.publishWorkOrderCompleted(workOrder, traceId);
            }
            default -> {
                // For intermediate states like DIAGNOSING, WAITING_APPROVAL, EXECUTING
                repository.save(workOrder);
            }
        }

        return workOrder;
    }
}
