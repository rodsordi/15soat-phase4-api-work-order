package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.port.in.CreateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.DeleteCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import br.com.fiap.phase4.workorder.domain.port.out.WorkOrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService implements CreateCustomerUseCase, GetCustomerUseCase, UpdateCustomerUseCase, DeleteCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;
    private final WorkOrderRepositoryPort workOrderRepository;

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

    @Override
    public Customer updateCustomer(UUID id, String name, String email) {
        Customer customer = getById(id);
        customer.update(name, email);
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomerById(UUID id) {
        Customer customer = getById(id);
        validateCustomerCanBeDeleted(customer);
        customerRepository.delete(customer);
    }

    private void validateCustomerCanBeDeleted(Customer customer) {
        if (!customer.getVehicles().isEmpty()) {
            throw new DomainException("CUSTOMER_HAS_VEHICLES",
                    "Customer has associated vehicles and cannot be deleted. Remove vehicles first.");
        }
        if (workOrderRepository.hasActiveWorkOrdersByCustomerId(customer.getId())) {
            throw new DomainException("CUSTOMER_HAS_ACTIVE_WORK_ORDERS",
                    "Customer has active work orders in progress and cannot be deleted.");
        }
    }
}
