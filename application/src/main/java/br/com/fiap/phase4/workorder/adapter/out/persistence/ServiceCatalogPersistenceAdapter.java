package br.com.fiap.phase4.workorder.adapter.out.persistence;

import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataServiceCatalogRepository;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.port.out.ServiceCatalogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServiceCatalogPersistenceAdapter implements ServiceCatalogRepositoryPort {

    private final SpringDataServiceCatalogRepository repository;

    @Override
    public ServiceCatalog save(ServiceCatalog serviceCatalog) {
        return repository.save(serviceCatalog);
    }

    @Override
    public Optional<ServiceCatalog> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ServiceCatalog> findByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public List<ServiceCatalog> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
