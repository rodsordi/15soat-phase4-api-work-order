package br.com.fiap.phase4.workorder.domain.port.in;

import java.util.UUID;

public interface DeleteCustomerUseCase {

    void deleteCustomerById(UUID id);
}
