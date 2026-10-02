package br.com.fiap.phase4.workorder.adapter.out.persistence.repository;

import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataServiceCatalogRepository extends JpaRepository<ServiceCatalog, UUID> {
    Optional<ServiceCatalog> findByCode(String code);
    boolean existsByCode(String code);
}
