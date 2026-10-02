package br.com.fiap.phase4.workorder.adapter.out.persistence;

import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataVehicleRepository;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehiclePersistenceAdapter implements VehicleRepositoryPort {

    private final SpringDataVehicleRepository repository;

    @Override
    public Vehicle save(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return repository.findByLicensePlate(licensePlate);
    }

    @Override
    public List<Vehicle> findByCustomerId(UUID customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public List<Vehicle> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        return repository.existsByLicensePlate(licensePlate);
    }
}
