package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Material;

public interface CreateMaterialUseCase {
    Material createMaterial(String sku, String name, String description,
                            Money unitCost, Money unitPrice, Integer initialStock, Integer minStock);
}
