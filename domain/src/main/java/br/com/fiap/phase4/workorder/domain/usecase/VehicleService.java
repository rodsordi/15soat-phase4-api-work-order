package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.in.CreateVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService implements CreateVehicleUseCase, GetVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepository;
    private final CustomerRepositoryPort customerRepository;

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
}
