package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMaterialRepository extends JpaRepository<Material, UUID> {
    Optional<Material> findBySku(String sku);
    boolean existsBySku(String sku);
}
