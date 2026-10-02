package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;

import java.util.UUID;

public interface GetServiceCatalogUseCase {
    ServiceCatalog getById(UUID id);
    ServiceCatalog getByCode(String code);
}
