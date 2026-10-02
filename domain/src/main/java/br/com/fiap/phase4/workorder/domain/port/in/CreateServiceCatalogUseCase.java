package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;

public interface CreateServiceCatalogUseCase {
    ServiceCatalog createService(String code, String name, String description,
                                 Money price, Long estimatedMinutes);
}
