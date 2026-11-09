package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.adapter.in.web.api.ServiceCatalogApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateServiceCatalogRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PagedServiceCatalogResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PatchServiceCatalogRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.ServiceAverageTimeMetricResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.ServiceCatalogDetailedResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.ServiceCatalogSummaryResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.ServiceMetricsResponse;
import br.com.fiap.phase4.workorder.domain.model.ServiceAverageTimeMetric;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.port.in.CreateServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetWorkOrderMetricsUseCase;
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
    private final GetWorkOrderMetricsUseCase getWorkOrderMetricsUseCase;

    @Override
    public ResponseEntity<ServiceCatalogDetailedResponse> createService(CreateServiceCatalogRequest request) {
        log.info("Received request to register service: {}", request.getCode());
        ServiceCatalog serviceCatalog = createServiceCatalogUseCase.createService(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                Money.of(request.getPrice()),
                0L
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailedResponse(serviceCatalog));
    }

    @Override
    public ResponseEntity<ServiceCatalogDetailedResponse> getServiceById(UUID id) {
        log.info("Received request to get service by ID: {}", id);
        ServiceCatalog serviceCatalog = getServiceCatalogUseCase.getById(id);
        return ResponseEntity.ok(toDetailedResponse(serviceCatalog));
    }

    @Override
    public ResponseEntity<ServiceMetricsResponse> getServiceMetrics(String serviceCode) {
        log.info("Received request to get service metrics for code: {}", serviceCode);
        List<ServiceAverageTimeMetric> metrics = getWorkOrderMetricsUseCase.calculateAndGetAverageExecutionTimes();
        if (serviceCode != null && !serviceCode.isBlank()) {
            metrics = metrics.stream()
                    .filter(m -> serviceCode.equalsIgnoreCase(m.serviceCode()))
                    .toList();
        }

        ServiceMetricsResponse response = new ServiceMetricsResponse();
        response.setServices(metrics.stream().map(m -> {
            ServiceAverageTimeMetricResponse item = new ServiceAverageTimeMetricResponse();
            item.setServiceId(m.serviceId());
            item.setServiceCode(m.serviceCode());
            item.setServiceName(m.serviceName());
            item.setAverageMinutes(m.averageMinutes());
            item.setTotalCompletedOrders(m.totalCompletedOrders());
            return item;
        }).toList());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PagedServiceCatalogResponse> listServices(
            String code, OffsetDateTime createdFrom, OffsetDateTime createdTo,
            Integer page, Integer size, List<String> sort) {
        log.info("Received request to list services with filter code: {}", code);
        List<ServiceCatalog> list;
        if (code != null && !code.isBlank()) {
            try {
                ServiceCatalog sc = getServiceCatalogUseCase.getByCode(code);
                list = List.of(sc);
            } catch (ResourceNotFoundException e) {
                list = List.of();
            }
        } else {
            list = listServicesCatalogUseCase.listAll();
        }

        PagedServiceCatalogResponse response = new PagedServiceCatalogResponse();
        response.setContent(list.stream().map(this::toSummaryResponse).toList());
        response.setPage(page != null ? page : 0);
        response.setSize(size != null ? size : 20);
        response.setTotalElements((long) list.size());
        response.setTotalPages(list.isEmpty() ? 0 : 1);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ServiceCatalogDetailedResponse> updateService(UUID id, PatchServiceCatalogRequest request) {
        log.info("Received request to update service ID: {}", id);
        Money newPrice = request.getPrice() != null ? Money.of(request.getPrice()) : null;
        ServiceCatalog updated = updateServiceCatalogUseCase.updateService(
                id,
                request.getName(),
                request.getDescription(),
                newPrice,
                request.getEstimatedMinutes()
        );
        return ResponseEntity.ok(toDetailedResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteServiceById(UUID id) {
        log.info("Received request to delete service ID: {}", id);
        updateServiceCatalogUseCase.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    private ServiceCatalogDetailedResponse toDetailedResponse(ServiceCatalog domain) {
        ServiceCatalogDetailedResponse response = new ServiceCatalogDetailedResponse();
        response.setId(domain.getId());
        response.setCode(domain.getCode());
        response.setName(domain.getName());
        response.setDescription(domain.getDescription());
        if (domain.getPrice() != null) {
            response.setPrice(domain.getPrice().amount());
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

    private ServiceCatalogSummaryResponse toSummaryResponse(ServiceCatalog domain) {
        ServiceCatalogSummaryResponse summary = new ServiceCatalogSummaryResponse();
        summary.setId(domain.getId());
        summary.setCode(domain.getCode());
        summary.setName(domain.getName());
        if (domain.getPrice() != null) {
            summary.setPrice(domain.getPrice().amount());
        }
        summary.setEstimatedMinutes(domain.getEstimatedMinutes());
        return summary;
    }
}
