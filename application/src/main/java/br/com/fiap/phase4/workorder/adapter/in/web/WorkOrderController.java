package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.workorder.adapter.in.web.api.WorkOrderApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateWorkOrderRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PagedWorkOrderResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.UpdateWorkOrderStatusRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderDetailedResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderMaterialItemResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderServiceItemResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderStatus;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderStatusHistoryResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderSummaryResponse;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.port.in.CreateWorkOrderUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetWorkOrderUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateWorkOrderStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
public class WorkOrderController implements WorkOrderApi {

    private final CreateWorkOrderUseCase createWorkOrderUseCase;
    private final GetWorkOrderUseCase getWorkOrderUseCase;
    private final UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase;

    @Override
    public ResponseEntity<WorkOrderDetailedResponse> createWorkOrder(CreateWorkOrderRequest request) {
        log.info("Received request to create work order for vehicle: {}", request.getLicensePlate());
        WorkOrder workOrder = createWorkOrderUseCase.create(
                request.getCustomerDocument(),
                request.getLicensePlate(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailedResponse(workOrder));
    }

    @Override
    public ResponseEntity<WorkOrderDetailedResponse> getWorkOrderById(UUID id) {
        log.info("Received request to get work order by ID: {}", id);
        WorkOrder workOrder = getWorkOrderUseCase.getById(id);
        return ResponseEntity.ok(toDetailedResponse(workOrder));
    }

    @Override
    public ResponseEntity<PagedWorkOrderResponse> listWorkOrders(
            WorkOrderStatus status, String customerDocument, String licensePlate,
            OffsetDateTime createdFrom, OffsetDateTime createdTo,
            Integer page, Integer size, List<String> sort) {
        log.info("Received request to list work orders with filter status: {}", status);
        br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus domainStatus = null;
        if (status != null) {
            domainStatus = br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus.valueOf(status.name());
        }
        List<WorkOrder> list = getWorkOrderUseCase.listByFilters(domainStatus, customerDocument, licensePlate);

        PagedWorkOrderResponse response = new PagedWorkOrderResponse();
        response.setContent(list.stream().map(this::toSummaryResponse).toList());
        response.setPage(page != null ? page : 0);
        response.setSize(size != null ? size : 20);
        response.setTotalElements((long) list.size());
        response.setTotalPages(list.isEmpty() ? 0 : 1);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WorkOrderDetailedResponse> updateWorkOrderStatus(UUID id, UpdateWorkOrderStatusRequest request) {
        log.info("Received request to update status for work order: {}", id);
        br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus domainStatus =
                br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus.valueOf(request.getStatus().name());
        String traceId = MDC.get("traceId");
        WorkOrder updated = updateWorkOrderStatusUseCase.updateStatus(id, domainStatus, request.getReason(), traceId);
        return ResponseEntity.ok(toDetailedResponse(updated));
    }

    private WorkOrderDetailedResponse toDetailedResponse(WorkOrder domain) {
        WorkOrderDetailedResponse response = new WorkOrderDetailedResponse();
        response.setId(domain.getId());
        response.setCustomerDocument(domain.getCustomerDocument().value());
        response.setLicensePlate(domain.getLicensePlate().value());
        response.setStatus(WorkOrderStatus.fromValue(domain.getStatus().name()));
        response.setDescription(domain.getDescription());
        response.setTotalAmount(domain.getTotalAmount().amount());
        if (domain.getServices() != null) {
            response.setServices(domain.getServices().stream().map(s -> {
                var dto = new WorkOrderServiceItemResponse();
                dto.setId(s.getId());
                dto.setServiceId(s.getServiceId());
                dto.setItemName(s.getItemName());
                dto.setItemCode(s.getItemCode());
                if (s.getUnitPrice() != null) {
                    dto.setUnitPrice(s.getUnitPrice().amount());
                }
                dto.setQuantity(s.getQuantity());
                if (s.getSubtotal() != null) {
                    dto.setSubtotal(s.getSubtotal().amount());
                }
                if (s.getCreatedAt() != null) {
                    dto.setCreatedAt(OffsetDateTime.ofInstant(s.getCreatedAt(), ZoneOffset.UTC));
                }
                return dto;
            }).toList());
        }
        if (domain.getMaterials() != null) {
            response.setMaterials(domain.getMaterials().stream().map(m -> {
                var dto = new WorkOrderMaterialItemResponse();
                dto.setId(m.getId());
                dto.setMaterialId(m.getMaterialId());
                dto.setItemName(m.getItemName());
                dto.setItemSku(m.getItemSku());
                if (m.getUnitPrice() != null) {
                    dto.setUnitPrice(m.getUnitPrice().amount());
                }
                dto.setQuantity(m.getQuantity());
                if (m.getSubtotal() != null) {
                    dto.setSubtotal(m.getSubtotal().amount());
                }
                if (m.getCreatedAt() != null) {
                    dto.setCreatedAt(OffsetDateTime.ofInstant(m.getCreatedAt(), ZoneOffset.UTC));
                }
                return dto;
            }).toList());
        }
        if (domain.getStatusHistory() != null) {
            response.setStatusHistory(domain.getStatusHistory().stream().map(h -> {
                var dto = new WorkOrderStatusHistoryResponse();
                dto.setId(h.getId());
                if (h.getFromStatus() != null) {
                    dto.setFromStatus(WorkOrderStatus.fromValue(h.getFromStatus().name()));
                }
                dto.setToStatus(WorkOrderStatus.fromValue(h.getToStatus().name()));
                dto.setReason(h.getReason());
                dto.setChangedBy(h.getChangedBy());
                if (h.getChangedAt() != null) {
                    dto.setChangedAt(OffsetDateTime.ofInstant(h.getChangedAt(), ZoneOffset.UTC));
                }
                return dto;
            }).toList());
        }
        if (domain.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        if (domain.getUpdatedAt() != null) {
            response.setUpdatedAt(OffsetDateTime.ofInstant(domain.getUpdatedAt(), ZoneOffset.UTC));
        }
        return response;
    }

    private WorkOrderSummaryResponse toSummaryResponse(WorkOrder domain) {
        WorkOrderSummaryResponse summary = new WorkOrderSummaryResponse();
        summary.setId(domain.getId());
        summary.setCustomerDocument(domain.getCustomerDocument().value());
        summary.setLicensePlate(domain.getLicensePlate().value());
        summary.setStatus(WorkOrderStatus.fromValue(domain.getStatus().name()));
        summary.setTotalAmount(domain.getTotalAmount().amount());
        if (domain.getCreatedAt() != null) {
            summary.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        return summary;
    }
}
