package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.WorkOrder;

public interface CreateWorkOrderUseCase {

    WorkOrder create(String customerDocument, String licensePlate, String description);
}
