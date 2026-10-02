package br.com.fiap.phase4.workorder.adapter.out.persistence;

import br.com.fiap.phase4.workorder.adapter.out.persistence.repository.SpringDataMaterialRepository;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MaterialPersistenceAdapter implements MaterialRepositoryPort {

    private final SpringDataMaterialRepository repository;

    @Override
    public Material save(Material material) {
        return repository.save(material);
    }

    @Override
    public Optional<Material> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Material> findBySku(String sku) {
        return repository.findBySku(sku);
    }

    @Override
    public List<Material> findAll() {
        return repository.findAll();
    }

    @Override
    public boolean existsBySku(String sku) {
        return repository.existsBySku(sku);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
