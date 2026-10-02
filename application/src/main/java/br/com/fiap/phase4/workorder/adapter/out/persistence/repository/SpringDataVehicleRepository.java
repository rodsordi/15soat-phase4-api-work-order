package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataVehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByCustomerId(UUID customerId);
    boolean existsByLicensePlate(String licensePlate);
}
