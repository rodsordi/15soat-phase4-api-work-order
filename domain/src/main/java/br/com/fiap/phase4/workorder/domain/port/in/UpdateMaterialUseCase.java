package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Material;

import java.util.UUID;

public interface UpdateMaterialUseCase {
    Material updatePrice(UUID id, Money newPrice, Money newCost);
    Material updateStock(UUID id, int stockDelta);
    Material updateDetails(UUID id, String name, String description, Integer minStock);
    void deleteMaterial(UUID id);
}
