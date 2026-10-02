package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.AlreadyExistsException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.port.in.CreateMaterialUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetMaterialUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.ListMaterialsUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateMaterialUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService implements CreateMaterialUseCase, GetMaterialUseCase, ListMaterialsUseCase, UpdateMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;

    @Override
    public Material createMaterial(String sku, String name, String description,
                                  Money unitCost, Money unitPrice, Integer initialStock, Integer minStock) {
        if (materialRepository.existsBySku(sku.trim().toUpperCase())) {
            throw new AlreadyExistsException("Material with SKU " + sku + " already exists");
        }
        Material material = Material.create(sku, name, description, unitCost, unitPrice, initialStock, minStock);
        return materialRepository.save(material);
    }

    @Override
    public Material getById(UUID id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with id: " + id));
    }

    @Override
    public Material getBySku(String sku) {
        return materialRepository.findBySku(sku.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Material not found with SKU: " + sku));
    }

    @Override
    public List<Material> listAll() {
        return materialRepository.findAll();
    }

    @Override
    public Material updatePrice(UUID id, Money newPrice, Money newCost) {
        Material material = getById(id);
        material.updatePrice(newPrice, newCost);
        return materialRepository.save(material);
    }

    @Override
    public Material updateStock(UUID id, int stockDelta) {
        Material material = getById(id);
        material.adjustStock(stockDelta);
        return materialRepository.save(material);
    }

    @Override
    public Material updateDetails(UUID id, String name, String description, Integer minStock) {
        Material material = getById(id);
        material.updateDetails(name, description, minStock);
        return materialRepository.save(material);
    }

    @Override
    public void deleteMaterial(UUID id) {
        if (materialRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Material not found with id: " + id);
        }
        materialRepository.deleteById(id);
    }
}
