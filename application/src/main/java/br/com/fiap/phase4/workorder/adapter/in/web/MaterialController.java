package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.adapter.in.web.api.MaterialApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateMaterialRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.MaterialResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.UpdateMaterialPriceRequest;
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
    public ResponseEntity<MaterialResponse> createMaterial(CreateMaterialRequest request) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(material));
    }

    @Override
    public ResponseEntity<MaterialResponse> getMaterialById(UUID id) {
        log.info("Received request to get material by id: {}", id);
        Material material = getMaterialUseCase.getById(id);
        return ResponseEntity.ok(toResponse(material));
    }

    @Override
    public ResponseEntity<List<MaterialResponse>> listMaterials(String search) {
        log.info("Received request to list materials with filter search: {}", search);
        if (search != null && !search.isBlank()) {
            Material material = getMaterialUseCase.getBySku(search);
            return ResponseEntity.ok(List.of(toResponse(material)));
        }
        List<MaterialResponse> list = listMaterialsUseCase.listAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<MaterialResponse> updateMaterialPrice(UUID id, UpdateMaterialPriceRequest request) {
        log.info("Received request to update price for material: {}", id);
        Money newPrice = Money.of(request.getUnitPrice());
        Money newCost = request.getUnitCost() != null ? Money.of(request.getUnitCost()) : null;
        Material updated = updateMaterialUseCase.updatePrice(id, newPrice, newCost);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteMaterial(UUID id) {
        log.info("Received request to delete material: {}", id);
        updateMaterialUseCase.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }

    private MaterialResponse toResponse(Material domain) {
        MaterialResponse response = new MaterialResponse();
        response.setId(domain.getId());
        response.setSku(domain.getSku());
        response.setName(domain.getName());
        response.setDescription(domain.getDescription());
        if (domain.getUnitCost() != null && domain.getUnitCost().amount() != null) {
            response.setUnitCost(domain.getUnitCost().amount().doubleValue());
        }
        if (domain.getUnitPrice() != null && domain.getUnitPrice().amount() != null) {
            response.setUnitPrice(domain.getUnitPrice().amount().doubleValue());
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
}
