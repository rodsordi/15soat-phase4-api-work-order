package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataVehicleRepository extends JpaRepository<Vehicle, UUID> {

    @EntityGraph(attributePaths = {"customer"})
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @EntityGraph(attributePaths = {"customer"})
    Optional<Vehicle> findById(UUID id);

    @EntityGraph(attributePaths = {"customer"})
    List<Vehicle> findByCustomerId(UUID customerId);

    @EntityGraph(attributePaths = {"customer"})
    List<Vehicle> findAll();

    boolean existsByLicensePlate(String licensePlate);
}
