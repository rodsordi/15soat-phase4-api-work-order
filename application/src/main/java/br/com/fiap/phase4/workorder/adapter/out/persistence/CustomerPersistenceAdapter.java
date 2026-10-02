package br.com.fiap.phase4.workorder.adapter.out.persistence;

import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataCustomerRepository;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final SpringDataCustomerRepository repository;

    @Override
    public Customer save(Customer customer) {
        return repository.save(customer);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Customer> findByDocument(String document) {
        return repository.findByDocument(document);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document);
    }
}
