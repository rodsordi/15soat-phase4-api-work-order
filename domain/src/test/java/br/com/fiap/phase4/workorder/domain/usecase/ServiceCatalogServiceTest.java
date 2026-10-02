package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.AlreadyExistsException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.port.out.ServiceCatalogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceCatalogService Use Case Unit Tests")
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepositoryPort serviceCatalogRepository;

    private ServiceCatalogService serviceCatalogService;

    @BeforeEach
    void setUp() {
        serviceCatalogService = new ServiceCatalogService(serviceCatalogRepository);
    }

    @Test
    @DisplayName("Should successfully create a new service catalog item when code is unique")
    void shouldCreateServiceCatalogWhenCodeIsUnique() {
        when(serviceCatalogRepository.existsByCode("SRV-OIL-01")).thenReturn(false);
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceCatalog result = serviceCatalogService.createService(
                "SRV-OIL-01",
                "Oil Change",
                "Engine oil change",
                Money.of(new BigDecimal("150.00")),
                30L
        );

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SRV-OIL-01");
        verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
    }

    @Test
    @DisplayName("Should throw AlreadyExistsException when code already exists")
    void shouldThrowWhenCodeExists() {
        when(serviceCatalogRepository.existsByCode("SRV-OIL-01")).thenReturn(true);

        assertThatThrownBy(() -> serviceCatalogService.createService(
                "SRV-OIL-01",
                "Oil Change",
                "Engine oil change",
                Money.of(new BigDecimal("150.00")),
                30L
        )).isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should retrieve service catalog item by code")
    void shouldRetrieveServiceByCode() {
        ServiceCatalog service = ServiceCatalog.create(
                "SRV-OIL-01",
                "Oil Change",
                "Engine oil change",
                Money.of(new BigDecimal("150.00")),
                30L
        );
        when(serviceCatalogRepository.findByCode("SRV-OIL-01")).thenReturn(Optional.of(service));

        ServiceCatalog found = serviceCatalogService.getByCode("SRV-OIL-01");
        assertThat(found.getCode()).isEqualTo("SRV-OIL-01");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when service not found")
    void shouldThrowWhenServiceNotFound() {
        UUID id = UUID.randomUUID();
        when(serviceCatalogRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCatalogService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
