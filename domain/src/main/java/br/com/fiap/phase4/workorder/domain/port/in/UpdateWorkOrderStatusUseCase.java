package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;

import java.util.UUID;

public interface UpdateWorkOrderStatusUseCase {

    WorkOrder updateStatus(UUID workOrderId, WorkOrderStatus newStatus, String reason, String traceId);
}
