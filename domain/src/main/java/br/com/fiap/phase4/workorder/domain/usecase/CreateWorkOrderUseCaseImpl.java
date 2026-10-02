package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
import br.com.fiap.phase4.workorder.domain.port.in.CreateWorkOrderUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateWorkOrderUseCaseImpl implements CreateWorkOrderUseCase {

    private final WorkOrderRepositoryPort repository;
    private final CustomerRepositoryPort customerRepository;
    private final VehicleRepositoryPort vehicleRepository;

    @Override
    public WorkOrder create(String customerDocument, String licensePlate, String description) {
        var cpfOrCnpj = new CpfOrCnpj(customerDocument);
        var plate = new LicensePlate(licensePlate);

        Customer customer = customerRepository != null ? customerRepository.findByDocument(cpfOrCnpj.value()).orElse(null) : null;
        Vehicle vehicle = vehicleRepository != null ? vehicleRepository.findByLicensePlate(plate.value()).orElse(null) : null;

        var workOrder = new WorkOrder(
                UUID.randomUUID(),
                customer,
                vehicle,
                cpfOrCnpj.value(),
                plate.value(),
                description,
                WorkOrderStatus.RECEIVED,
                BigDecimal.ZERO,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now()
        );
        return repository.save(workOrder);
    }
}
