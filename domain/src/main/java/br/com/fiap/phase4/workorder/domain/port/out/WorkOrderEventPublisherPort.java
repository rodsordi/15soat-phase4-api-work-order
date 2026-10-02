package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.WorkOrder;

public interface WorkOrderEventPublisherPort {

    void publishWorkOrderApproved(WorkOrder workOrder, String traceId);

    void publishWorkOrderCompleted(WorkOrder workOrder, String traceId);
}
