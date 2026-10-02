package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.AlreadyExistsException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.port.in.CreateServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.ListServicesCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateServiceCatalogUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.ServiceCatalogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService implements CreateServiceCatalogUseCase, GetServiceCatalogUseCase,
        ListServicesCatalogUseCase, UpdateServiceCatalogUseCase {

    private final ServiceCatalogRepositoryPort serviceCatalogRepository;

    @Override
    public ServiceCatalog createService(String code, String name, String description,
                                        Money price, Long estimatedMinutes) {
        if (serviceCatalogRepository.existsByCode(code.trim().toUpperCase())) {
            throw new AlreadyExistsException("Service with code " + code + " already exists");
        }
        ServiceCatalog serviceCatalog = ServiceCatalog.create(code, name, description, price, estimatedMinutes);
        return serviceCatalogRepository.save(serviceCatalog);
    }

    @Override
    public ServiceCatalog getById(UUID id) {
        return serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
    }

    @Override
    public ServiceCatalog getByCode(String code) {
        return serviceCatalogRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with code: " + code));
    }

    @Override
    public List<ServiceCatalog> listAll() {
        return serviceCatalogRepository.findAll();
    }

    @Override
    public ServiceCatalog updatePrice(UUID id, Money newPrice) {
        ServiceCatalog serviceCatalog = getById(id);
        serviceCatalog.updatePrice(newPrice);
        return serviceCatalogRepository.save(serviceCatalog);
    }

    @Override
    public ServiceCatalog updateDetails(UUID id, String name, String description, Long estimatedMinutes) {
        ServiceCatalog serviceCatalog = getById(id);
        serviceCatalog.updateDetails(name, description, estimatedMinutes);
        return serviceCatalogRepository.save(serviceCatalog);
    }

    @Override
    public void deleteService(UUID id) {
        if (serviceCatalogRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Service not found with id: " + id);
        }
        serviceCatalogRepository.deleteById(id);
    }
}
