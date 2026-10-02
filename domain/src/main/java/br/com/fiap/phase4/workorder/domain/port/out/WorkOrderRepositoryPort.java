package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkOrderRepositoryPort {

    WorkOrder save(WorkOrder workOrder);

    Optional<WorkOrder> findById(UUID id);

    List<WorkOrder> findByFilters(WorkOrderStatus status, String customerDocument, String licensePlate);
}
