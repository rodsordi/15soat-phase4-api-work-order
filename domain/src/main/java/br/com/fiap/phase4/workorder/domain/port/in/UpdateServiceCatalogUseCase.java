package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;

import java.util.UUID;

public interface UpdateServiceCatalogUseCase {
    ServiceCatalog updatePrice(UUID id, Money newPrice);
    ServiceCatalog updateDetails(UUID id, String name, String description, Long estimatedMinutes);
    void deleteService(UUID id);
}
