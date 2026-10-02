package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepositoryPort {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(UUID id);
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByCustomerId(UUID customerId);
    List<Vehicle> findAll();
    boolean existsByLicensePlate(String licensePlate);
}
