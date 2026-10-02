package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.workorder.adapter.in.web.api.CustomerApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateCustomerRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CustomerResponse;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.port.in.CreateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetCustomerUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        log.info("Received request to create customer with document: {}", request.getDocument());
        Customer customer = createCustomerUseCase.createCustomer(request.getName(), request.getEmail(), request.getDocument());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(customer));
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerByDocument(String document) {
        log.info("Received request to get customer by document: {}", document);
        Customer customer = getCustomerUseCase.getByDocument(document);
        return ResponseEntity.ok(toResponse(customer));
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setDocument(customer.getDocument());
        if (customer.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(customer.getCreatedAt(), ZoneOffset.UTC));
        }
        return response;
    }
}
