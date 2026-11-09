package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCustomerRepository extends JpaRepository<Customer, UUID> {

    @EntityGraph(attributePaths = {"vehicles"})
    Optional<Customer> findByDocument(String document);

    @EntityGraph(attributePaths = {"vehicles"})
    Optional<Customer> findByEmail(String email);

    @EntityGraph(attributePaths = {"vehicles"})
    Optional<Customer> findById(UUID id);

    @EntityGraph(attributePaths = {"vehicles"})
    List<Customer> findAll();

    boolean existsByDocument(String document);
}
