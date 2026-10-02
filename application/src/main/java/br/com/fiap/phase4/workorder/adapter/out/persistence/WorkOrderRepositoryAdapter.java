package br.com.fiap.phase4.workorder.adapter.out.persistence;

import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataWorkOrderRepository;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkOrderRepositoryAdapter implements WorkOrderRepositoryPort {

    private final SpringDataWorkOrderRepository repository;

    @Override
    public WorkOrder save(WorkOrder workOrder) {
        return repository.save(workOrder);
    }

    @Override
    public Optional<WorkOrder> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<WorkOrder> findByFilters(WorkOrderStatus status, String customerDocument, String licensePlate) {
        return repository.findByFilters(status, customerDocument, licensePlate);
    }
}
