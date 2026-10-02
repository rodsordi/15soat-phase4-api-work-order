package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.adapter.in.web.api.ServiceCatalogApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateServiceCatalogRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.ServiceCatalogResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.UpdateServiceCatalogPriceRequest;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.port.in.CreateServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.ListServicesCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateServiceCatalogUseCase;
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
public class ServiceCatalogController implements ServiceCatalogApi {

    private final CreateServiceCatalogUseCase createServiceCatalogUseCase;
    private final GetServiceCatalogUseCase getServiceCatalogUseCase;
    private final ListServicesCatalogUseCase listServicesCatalogUseCase;
    private final UpdateServiceCatalogUseCase updateServiceCatalogUseCase;

    @Override
    public ResponseEntity<ServiceCatalogResponse> createService(CreateServiceCatalogRequest request) {
        log.info("Received request to register service: {}", request.getCode());
        ServiceCatalog serviceCatalog = createServiceCatalogUseCase.createService(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                Money.of(request.getPrice()),
                request.getEstimatedMinutes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(serviceCatalog));
    }

    @Override
    public ResponseEntity<ServiceCatalogResponse> getServiceById(UUID id) {
        log.info("Received request to get service by id: {}", id);
        ServiceCatalog serviceCatalog = getServiceCatalogUseCase.getById(id);
        return ResponseEntity.ok(toResponse(serviceCatalog));
    }

    @Override
    public ResponseEntity<List<ServiceCatalogResponse>> listServices(String search) {
        log.info("Received request to list services with filter search: {}", search);
        if (search != null && !search.isBlank()) {
            ServiceCatalog serviceCatalog = getServiceCatalogUseCase.getByCode(search);
            return ResponseEntity.ok(List.of(toResponse(serviceCatalog)));
        }
        List<ServiceCatalogResponse> list = listServicesCatalogUseCase.listAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<ServiceCatalogResponse> updateServicePrice(UUID id, UpdateServiceCatalogPriceRequest request) {
        log.info("Received request to update price for service: {}", id);
        ServiceCatalog updated = updateServiceCatalogUseCase.updatePrice(id, Money.of(request.getPrice()));
        return ResponseEntity.ok(toResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteService(UUID id) {
        log.info("Received request to delete service: {}", id);
        updateServiceCatalogUseCase.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    private ServiceCatalogResponse toResponse(ServiceCatalog domain) {
        ServiceCatalogResponse response = new ServiceCatalogResponse();
        response.setId(domain.getId());
        response.setCode(domain.getCode());
        response.setName(domain.getName());
        response.setDescription(domain.getDescription());
        if (domain.getPrice() != null && domain.getPrice().amount() != null) {
            response.setPrice(domain.getPrice().amount().doubleValue());
        }
        response.setEstimatedMinutes(domain.getEstimatedMinutes());
        if (domain.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        if (domain.getUpdatedAt() != null) {
            response.setUpdatedAt(OffsetDateTime.ofInstant(domain.getUpdatedAt(), ZoneOffset.UTC));
        }
        return response;
    }
}
