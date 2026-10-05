package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderMaterialItem;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateWorkOrderStatusUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
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
    private final MaterialRepositoryPort materialRepository;

    @Override
    public WorkOrder updateStatus(UUID workOrderId, WorkOrderStatus newStatus, String reason, String traceId) {
        WorkOrder workOrder = repository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkOrder", workOrderId));

        WorkOrderStatus previousStatus = workOrder.getStatus();

        switch (newStatus) {
            case APPROVED -> {
                workOrder.approve();
                reserveMaterialsForWorkOrder(workOrder);
                repository.save(workOrder);
                eventPublisher.publishWorkOrderApproved(workOrder, traceId);
            }
            case CANCELED -> {
                workOrder.cancel(reason);
                if (previousStatus == WorkOrderStatus.APPROVED || previousStatus == WorkOrderStatus.EXECUTING) {
                    releaseReservedMaterialsForWorkOrder(workOrder);
                }
                repository.save(workOrder);
            }
            case PAYMENT_REJECTED -> {
                workOrder.rejectPayment(reason);
                if (previousStatus == WorkOrderStatus.APPROVED) {
                    releaseReservedMaterialsForWorkOrder(workOrder);
                }
                repository.save(workOrder);
            }
            case COMPLETED -> {
                workOrder.complete();
                concludeReservedMaterialsForWorkOrder(workOrder);
                repository.save(workOrder);
                eventPublisher.publishWorkOrderCompleted(workOrder, traceId);
            }
            case DELIVERED -> {
                workOrder.deliver();
                repository.save(workOrder);
            }
            default -> {
                workOrder.transitionTo(newStatus, reason, null);
                repository.save(workOrder);
            }
        }

        return workOrder;
    }

    private void reserveMaterialsForWorkOrder(WorkOrder workOrder) {
        if (materialRepository == null) return;
        for (WorkOrderMaterialItem item : workOrder.getMaterials()) {
            materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                material.reserveQuantity(item.getQuantity());
                materialRepository.save(material);
            });
        }
    }

    private void releaseReservedMaterialsForWorkOrder(WorkOrder workOrder) {
        if (materialRepository == null) return;
        for (WorkOrderMaterialItem item : workOrder.getMaterials()) {
            materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                material.releaseReservedQuantity(item.getQuantity());
                materialRepository.save(material);
            });
        }
    }

    private void concludeReservedMaterialsForWorkOrder(WorkOrder workOrder) {
        if (materialRepository == null) return;
        for (WorkOrderMaterialItem item : workOrder.getMaterials()) {
            materialRepository.findById(item.getMaterialId()).ifPresent(material -> {
                material.concludeReservedQuantity(item.getQuantity());
                materialRepository.save(material);
            });
        }
    }
}
