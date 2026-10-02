package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.Customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryPort {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByDocument(String document);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
    boolean existsByDocument(String document);
}
