package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Material;

import java.util.UUID;

public interface GetMaterialUseCase {
    Material getById(UUID id);
    Material getBySku(String sku);
}
