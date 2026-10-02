package br.com.fiap.phase4.workorder.domain.port.out;

import br.com.fiap.phase4.workorder.domain.model.Material;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaterialRepositoryPort {

    Material save(Material material);

    Optional<Material> findById(UUID id);

    Optional<Material> findBySku(String sku);

    List<Material> findAll();

    boolean existsBySku(String sku);

    void deleteById(UUID id);
}
