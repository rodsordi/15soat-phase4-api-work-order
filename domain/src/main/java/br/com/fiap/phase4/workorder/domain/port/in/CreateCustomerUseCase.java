package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Customer;

public interface CreateCustomerUseCase {
    Customer createCustomer(String name, String email, String document);
}
