package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.in.CreateVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.DeleteVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService implements CreateVehicleUseCase, GetVehicleUseCase, UpdateVehicleUseCase, DeleteVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepository;
    private final CustomerRepositoryPort customerRepository;
    private final WorkOrderRepositoryPort workOrderRepository;

    @Override
    public Vehicle createVehicle(String customerDocument, String licensePlate, String make, String model, Integer manufactureYear) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new DomainException("VEHICLE_ALREADY_EXISTS", "Vehicle already exists with license plate: " + licensePlate);
        }
        Customer customer = customerRepository.findByDocument(customerDocument)
                .orElseThrow(() -> new DomainException("CUSTOMER_NOT_FOUND", "Customer not found with document: " + customerDocument));

        Vehicle vehicle = Vehicle.create(customer, licensePlate, make, model, manufactureYear);
        customer.addVehicle(vehicle);
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle getById(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new DomainException("VEHICLE_NOT_FOUND", "Vehicle not found with id: " + id));
    }

    @Override
    public Vehicle getByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new DomainException("VEHICLE_NOT_FOUND", "Vehicle not found with license plate: " + licensePlate));
    }

    @Override
    public Vehicle updateVehicle(UUID id, String make, String model, Integer manufactureYear) {
        Vehicle vehicle = getById(id);
        vehicle.update(make, model, manufactureYear);
        return vehicleRepository.save(vehicle);
    }

    @Override
    public void deleteVehicleById(UUID id) {
        Vehicle vehicle = getById(id);
        validateVehicleCanBeDeleted(vehicle);
        vehicleRepository.delete(vehicle);
    }

    private void validateVehicleCanBeDeleted(Vehicle vehicle) {
        if (workOrderRepository.hasActiveWorkOrdersByVehicleId(vehicle.getId())) {
            throw new DomainException("VEHICLE_HAS_ACTIVE_WORK_ORDERS",
                    "Vehicle has active work orders in progress and cannot be deleted.");
        }
    }
}
