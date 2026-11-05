package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Customer;

import java.util.UUID;

public interface UpdateCustomerUseCase {
    Customer updateCustomer(UUID id, String name, String email);
}
