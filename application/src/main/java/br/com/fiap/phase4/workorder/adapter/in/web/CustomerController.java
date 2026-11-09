package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.adapter.in.web.api.CustomerApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateCustomerRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CustomerDetailedResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CustomerSummaryResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CustomerVehicleItemResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PagedCustomerResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PatchCustomerRequest;
import br.com.fiap.phase4.workorder.domain.model.Customer;
import br.com.fiap.phase4.workorder.domain.port.in.CreateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.DeleteCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateCustomerUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final CustomerRepositoryPort customerRepositoryPort;

    @Override
    public ResponseEntity<CustomerDetailedResponse> createCustomer(CreateCustomerRequest request) {
        log.info("Received request to create customer with document: {}", request.getDocument());
        Customer customer = createCustomerUseCase.createCustomer(request.getName(), request.getEmail(), request.getDocument());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailedResponse(customer));
    }

    @Override
    public ResponseEntity<CustomerDetailedResponse> getCustomerById(UUID id) {
        log.info("Received request to get customer by ID: {}", id);
        Customer customer = getCustomerUseCase.getById(id);
        return ResponseEntity.ok(toDetailedResponse(customer));
    }

    @Override
    public ResponseEntity<PagedCustomerResponse> listCustomers(
            String document, OffsetDateTime createdFrom, OffsetDateTime createdTo,
            Integer page, Integer size, List<String> sort) {
        log.info("Received request to list customers with filter document: {}", document);
        List<Customer> list;
        if (document != null && !document.isBlank()) {
            try {
                Customer customer = getCustomerUseCase.getByDocument(document);
                list = List.of(customer);
            } catch (DomainException e) {
                list = List.of();
            }
        } else {
            list = customerRepositoryPort.findAll();
        }

        PagedCustomerResponse response = new PagedCustomerResponse();
        response.setContent(list.stream().map(this::toSummaryResponse).toList());
        response.setPage(page != null ? page : 0);
        response.setSize(size != null ? size : 20);
        response.setTotalElements((long) list.size());
        response.setTotalPages(list.isEmpty() ? 0 : 1);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerDetailedResponse> updateCustomer(UUID id, PatchCustomerRequest request) {
        log.info("Received request to update customer ID: {}", id);
        Customer updated = updateCustomerUseCase.updateCustomer(id, request.getName(), request.getEmail());
        return ResponseEntity.ok(toDetailedResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteCustomerById(UUID id) {
        log.info("Received request to delete customer ID: {}", id);
        deleteCustomerUseCase.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }

    private CustomerDetailedResponse toDetailedResponse(Customer domain) {
        CustomerDetailedResponse response = new CustomerDetailedResponse();
        response.setId(domain.getId());
        response.setName(domain.getName());
        response.setEmail(domain.getEmail());
        response.setDocument(domain.getDocument());
        if (domain.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        if (domain.getUpdatedAt() != null) {
            response.setUpdatedAt(OffsetDateTime.ofInstant(domain.getUpdatedAt(), ZoneOffset.UTC));
        }
        if (domain.getVehicles() != null) {
            response.setVehicles(domain.getVehicles().stream().map(v -> {
                var item = new CustomerVehicleItemResponse();
                item.setId(v.getId());
                item.setLicensePlate(v.getLicensePlate());
                item.setMake(v.getMake());
                item.setModel(v.getModel());
                item.setManufactureYear(v.getManufactureYear());
                if (v.getCreatedAt() != null) {
                    item.setCreatedAt(OffsetDateTime.ofInstant(v.getCreatedAt(), ZoneOffset.UTC));
                }
                return item;
            }).toList());
        }
        return response;
    }

    private CustomerSummaryResponse toSummaryResponse(Customer domain) {
        CustomerSummaryResponse summary = new CustomerSummaryResponse();
        summary.setId(domain.getId());
        summary.setName(domain.getName());
        summary.setDocument(domain.getDocument());
        if (domain.getCreatedAt() != null) {
            summary.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        if (domain.getVehicles() != null) {
            summary.setVehicles(domain.getVehicles().stream().map(v -> {
                var item = new CustomerVehicleItemResponse();
                item.setId(v.getId());
                item.setLicensePlate(v.getLicensePlate());
                item.setMake(v.getMake());
                item.setModel(v.getModel());
                item.setManufactureYear(v.getManufactureYear());
                if (v.getCreatedAt() != null) {
                    item.setCreatedAt(OffsetDateTime.ofInstant(v.getCreatedAt(), ZoneOffset.UTC));
                }
                return item;
            }).toList());
        }
        return summary;
    }
}
