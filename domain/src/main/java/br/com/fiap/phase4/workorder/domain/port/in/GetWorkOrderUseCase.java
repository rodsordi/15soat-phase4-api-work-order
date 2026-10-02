package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;

import java.util.List;
import java.util.UUID;

public interface GetWorkOrderUseCase {

    WorkOrder getById(UUID id);

    List<WorkOrder> listByFilters(WorkOrderStatus status, String customerDocument, String licensePlate);
}
