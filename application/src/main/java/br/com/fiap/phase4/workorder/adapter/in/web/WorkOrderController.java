package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.workorder.adapter.in.web.api.WorkOrderApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateWorkOrderRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.UpdateStatusRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderResponse;
import br.com.fiap.phase4.workorder.domain.model.WorkOrder;
import br.com.fiap.phase4.workorder.domain.model.WorkOrderStatus;
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
    public ResponseEntity<WorkOrderResponse> createWorkOrder(CreateWorkOrderRequest request) {
        log.info("Received request to create work order for vehicle: {}", request.getLicensePlate());
        WorkOrder workOrder = createWorkOrderUseCase.create(
                request.getCustomerDocument(),
                request.getLicensePlate(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(workOrder));
    }

    @Override
    public ResponseEntity<WorkOrderResponse> getWorkOrderById(UUID id) {
        log.info("Received request to get work order by ID: {}", id);
        WorkOrder workOrder = getWorkOrderUseCase.getById(id);
        return ResponseEntity.ok(toResponse(workOrder));
    }

    @Override
    public ResponseEntity<List<WorkOrderResponse>> listWorkOrders(String status, String customerDocument, String licensePlate) {
        log.info("Received request to list work orders with filter status: {}", status);
        WorkOrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = WorkOrderStatus.fromString(status);
            } catch (IllegalArgumentException ignored) {
            }
        }
        List<WorkOrder> list = getWorkOrderUseCase.listByFilters(orderStatus, customerDocument, licensePlate);
        return ResponseEntity.ok(list.stream().map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<WorkOrderResponse> updateWorkOrderStatus(UUID id, UpdateStatusRequest request) {
        log.info("Received request to update status for work order: {}", id);
        WorkOrderStatus status = WorkOrderStatus.fromString(request.getStatus().name());
        String traceId = MDC.get("traceId");
        WorkOrder updated = updateWorkOrderStatusUseCase.updateStatus(id, status, request.getReason(), traceId);
        return ResponseEntity.ok(toResponse(updated));
    }

    private WorkOrderResponse toResponse(WorkOrder domain) {
        WorkOrderResponse response = new WorkOrderResponse();
        response.setId(domain.getId());
        response.setCustomerDocument(domain.getCustomerDocument().value());
        response.setLicensePlate(domain.getLicensePlate().value());
        response.setStatus(domain.getStatus().name());
        response.setDescription(domain.getDescription());
        response.setTotalAmount(domain.getTotalAmount().amount().doubleValue());
        if (domain.getServices() != null) {
            response.setServices(domain.getServices().stream().map(s -> {
                var dto = new br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderServiceItemResponse();
                dto.setId(s.getId());
                dto.setServiceId(s.getServiceId());
                dto.setItemName(s.getItemName());
                dto.setItemCode(s.getItemCode());
                dto.setUnitPrice(s.getUnitPrice().amount().doubleValue());
                dto.setQuantity(s.getQuantity());
                dto.setSubtotal(s.getSubtotal().amount().doubleValue());
                if (s.getCreatedAt() != null) {
                    dto.setCreatedAt(OffsetDateTime.ofInstant(s.getCreatedAt(), ZoneOffset.UTC));
                }
                return dto;
            }).toList());
        }
        if (domain.getMaterials() != null) {
            response.setMaterials(domain.getMaterials().stream().map(m -> {
                var dto = new br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderMaterialItemResponse();
                dto.setId(m.getId());
                dto.setMaterialId(m.getMaterialId());
                dto.setItemName(m.getItemName());
                dto.setItemSku(m.getItemSku());
                dto.setUnitPrice(m.getUnitPrice().amount().doubleValue());
                dto.setQuantity(m.getQuantity());
                dto.setSubtotal(m.getSubtotal().amount().doubleValue());
                if (m.getCreatedAt() != null) {
                    dto.setCreatedAt(OffsetDateTime.ofInstant(m.getCreatedAt(), ZoneOffset.UTC));
                }
                return dto;
            }).toList());
        }
        if (domain.getStatusHistory() != null) {
            response.setStatusHistory(domain.getStatusHistory().stream().map(h -> {
                var dto = new br.com.fiap.phase4.workorder.adapter.in.web.dto.WorkOrderStatusHistoryResponse();
                dto.setId(h.getId());
                if (h.getFromStatus() != null) {
                    dto.setFromStatus(h.getFromStatus().name());
                }
                dto.setToStatus(h.getToStatus().name());
                dto.setReason(h.getReason());
                dto.setChangedBy(h.getChangedBy());
                if (h.getChangedAt() != null) {
                    dto.setChangedAt(OffsetDateTime.ofInstant(h.getChangedAt(), ZoneOffset.UTC));
                }
                return dto;
            }).toList());
        }
        response.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        response.setUpdatedAt(OffsetDateTime.ofInstant(domain.getUpdatedAt(), ZoneOffset.UTC));
        return response;
    }
}
