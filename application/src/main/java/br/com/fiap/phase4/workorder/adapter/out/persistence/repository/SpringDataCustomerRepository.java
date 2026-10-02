package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByDocument(String document);
    Optional<Customer> findByEmail(String email);
    boolean existsByDocument(String document);
}
