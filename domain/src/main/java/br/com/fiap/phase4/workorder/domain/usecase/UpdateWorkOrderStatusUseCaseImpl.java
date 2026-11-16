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
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderStatusMetricsPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@Transactional
public class UpdateWorkOrderStatusUseCaseImpl implements UpdateWorkOrderStatusUseCase {

    private final WorkOrderRepositoryPort repository;
    private final WorkOrderEventPublisherPort eventPublisher;
    private final MaterialRepositoryPort materialRepository;
    private final WorkOrderStatusMetricsPort metricsPort;

    public UpdateWorkOrderStatusUseCaseImpl(WorkOrderRepositoryPort repository,
                                            WorkOrderEventPublisherPort eventPublisher,
                                            MaterialRepositoryPort materialRepository) {
        this(repository, eventPublisher, materialRepository, null);
    }

    @Autowired
    public UpdateWorkOrderStatusUseCaseImpl(WorkOrderRepositoryPort repository,
                                            WorkOrderEventPublisherPort eventPublisher,
                                            MaterialRepositoryPort materialRepository,
                                            @Autowired(required = false) WorkOrderStatusMetricsPort metricsPort) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.materialRepository = materialRepository;
        this.metricsPort = metricsPort;
    }

    @Override
    public WorkOrder updateStatus(UUID workOrderId, WorkOrderStatus newStatus, String reason, String traceId) {
        WorkOrder workOrder = repository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkOrder", workOrderId));

        WorkOrderStatus previousStatus = workOrder.getStatus();
        Duration durationInPreviousStatus = workOrder.calculateCurrentStatusDuration();

        switch (newStatus) {
            case DIAGNOSING -> {
                workOrder.startDiagnosis();
                repository.save(workOrder);
            }
            case WAITING_APPROVAL -> {
                workOrder.finishDiagnosisAndSendBudget();
                repository.save(workOrder);
                eventPublisher.publishWorkOrderEstimateRequested(workOrder, traceId);
            }
            case APPROVED -> {
                workOrder.approve();
                reserveMaterialsForWorkOrder(workOrder);
                repository.save(workOrder);
                eventPublisher.publishWorkOrderApproved(workOrder, traceId);
            }
            case EXECUTING -> {
                workOrder.startExecution();
                repository.save(workOrder);
            }
            case COMPLETED -> {
                workOrder.complete();
                concludeReservedMaterialsForWorkOrder(workOrder);
                repository.save(workOrder);
                eventPublisher.publishWorkOrderCompleted(workOrder, traceId);
            }
            case PAID -> {
                workOrder.markAsPaid();
                repository.save(workOrder);
            }
            case DELIVERED -> {
                workOrder.deliver();
                repository.save(workOrder);
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
            default -> {
                workOrder.transitionTo(newStatus, reason, null);
                repository.save(workOrder);
            }
        }

        if (metricsPort != null && previousStatus != null && durationInPreviousStatus != null && !durationInPreviousStatus.isNegative()) {
            metricsPort.recordStatusDuration(previousStatus, durationInPreviousStatus);
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
