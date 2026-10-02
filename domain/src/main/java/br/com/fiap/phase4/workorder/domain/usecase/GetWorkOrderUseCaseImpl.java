package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.in.GetWorkOrderUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWorkOrderUseCaseImpl implements GetWorkOrderUseCase {

    private final WorkOrderRepositoryPort repository;

    @Override
    public WorkOrder getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkOrder", id));
    }

    @Override
    public List<WorkOrder> listByFilters(WorkOrderStatus status, String customerDocument, String licensePlate) {
        return repository.findByFilters(status, customerDocument, licensePlate);
    }
}
