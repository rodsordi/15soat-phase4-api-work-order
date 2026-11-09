package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.adapter.in.web.api.MaterialApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateMaterialRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.MaterialDetailedResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.MaterialSummaryResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PagedMaterialResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PatchMaterialRequest;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.port.in.CreateMaterialUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetMaterialUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.ListMaterialsUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateMaterialUseCase;
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
public class MaterialController implements MaterialApi {

    private final CreateMaterialUseCase createMaterialUseCase;
    private final GetMaterialUseCase getMaterialUseCase;
    private final ListMaterialsUseCase listMaterialsUseCase;
    private final UpdateMaterialUseCase updateMaterialUseCase;

    @Override
    public ResponseEntity<MaterialDetailedResponse> createMaterial(CreateMaterialRequest request) {
        log.info("Received request to register material: {}", request.getSku());
        Material material = createMaterialUseCase.createMaterial(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getUnitCost() != null ? Money.of(request.getUnitCost()) : Money.of(0.0),
                Money.of(request.getUnitPrice()),
                request.getInitialStock() != null ? request.getInitialStock() : 0,
                request.getMinStock() != null ? request.getMinStock() : 0
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailedResponse(material));
    }

    @Override
    public ResponseEntity<MaterialDetailedResponse> getMaterialById(UUID id) {
        log.info("Received request to get material by ID: {}", id);
        Material material = getMaterialUseCase.getById(id);
        return ResponseEntity.ok(toDetailedResponse(material));
    }

    @Override
    public ResponseEntity<PagedMaterialResponse> listMaterials(
            String sku, OffsetDateTime createdFrom, OffsetDateTime createdTo,
            Integer page, Integer size, List<String> sort) {
        log.info("Received request to list materials with filter SKU: {}", sku);
        List<Material> list;
        if (sku != null && !sku.isBlank()) {
            try {
                Material m = getMaterialUseCase.getBySku(sku);
                list = List.of(m);
            } catch (ResourceNotFoundException e) {
                list = List.of();
            }
        } else {
            list = listMaterialsUseCase.listAll();
        }

        PagedMaterialResponse response = new PagedMaterialResponse();
        response.setContent(list.stream().map(this::toSummaryResponse).toList());
        response.setPage(page != null ? page : 0);
        response.setSize(size != null ? size : 20);
        response.setTotalElements((long) list.size());
        response.setTotalPages(list.isEmpty() ? 0 : 1);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MaterialDetailedResponse> updateMaterial(UUID id, PatchMaterialRequest request) {
        log.info("Received request to update material ID: {}", id);
        Money newPrice = request.getUnitPrice() != null ? Money.of(request.getUnitPrice()) : null;
        Money newCost = request.getUnitCost() != null ? Money.of(request.getUnitCost()) : null;
        Material updated = updateMaterialUseCase.updateMaterial(
                id,
                request.getName(),
                request.getDescription(),
                newCost,
                newPrice,
                request.getMinStock()
        );
        return ResponseEntity.ok(toDetailedResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteMaterialById(UUID id) {
        log.info("Received request to delete material ID: {}", id);
        updateMaterialUseCase.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    private MaterialDetailedResponse toDetailedResponse(Material domain) {
        MaterialDetailedResponse response = new MaterialDetailedResponse();
        response.setId(domain.getId());
        response.setSku(domain.getSku());
        response.setName(domain.getName());
        response.setDescription(domain.getDescription());
        if (domain.getUnitCost() != null) {
            response.setUnitCost(domain.getUnitCost().amount());
        }
        if (domain.getUnitPrice() != null) {
            response.setUnitPrice(domain.getUnitPrice().amount());
        }
        response.setStockQuantity(domain.getStockQuantity());
        response.setReservedQuantity(domain.getReservedQuantity());
        response.setAvailableQuantity(domain.getAvailableQuantity());
        response.setMinStock(domain.getMinStock());
        if (domain.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        if (domain.getUpdatedAt() != null) {
            response.setUpdatedAt(OffsetDateTime.ofInstant(domain.getUpdatedAt(), ZoneOffset.UTC));
        }
        return response;
    }

    private MaterialSummaryResponse toSummaryResponse(Material domain) {
        MaterialSummaryResponse summary = new MaterialSummaryResponse();
        summary.setId(domain.getId());
        summary.setSku(domain.getSku());
        summary.setName(domain.getName());
        if (domain.getUnitPrice() != null) {
            summary.setUnitPrice(domain.getUnitPrice().amount());
        }
        summary.setAvailableQuantity(domain.getAvailableQuantity());
        return summary;
    }
}
