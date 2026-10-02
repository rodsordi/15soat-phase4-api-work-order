package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCatalogRepositoryPort {

    ServiceCatalog save(ServiceCatalog serviceCatalog);

    Optional<ServiceCatalog> findById(UUID id);

    Optional<ServiceCatalog> findByCode(String code);

    List<ServiceCatalog> findAll();

    boolean existsByCode(String code);

    void deleteById(UUID id);
}
