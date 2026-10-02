package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.port.in.CreateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements CreateCustomerUseCase, GetCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    public Customer createCustomer(String name, String email, String document) {
        if (customerRepository.existsByDocument(document)) {
            throw new DomainException("CUSTOMER_ALREADY_EXISTS", "Customer already exists with document: " + document);
        }
        Customer customer = Customer.create(name, email, document);
        return customerRepository.save(customer);
    }

    @Override
    public Customer getById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new DomainException("CUSTOMER_NOT_FOUND", "Customer not found with id: " + id));
    }

    @Override
    public Customer getByDocument(String document) {
        return customerRepository.findByDocument(document)
                .orElseThrow(() -> new DomainException("CUSTOMER_NOT_FOUND", "Customer not found with document: " + document));
    }
}
