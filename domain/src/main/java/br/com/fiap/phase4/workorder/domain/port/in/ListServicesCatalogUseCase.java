package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;

import java.util.List;

public interface ListServicesCatalogUseCase {
    List<ServiceCatalog> listAll();
}
