package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.AlreadyExistsException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.ServiceCatalog;
import br.com.fiap.phase4.workorder.domain.port.out.ServiceCatalogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceCatalogService Unit Tests")
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepositoryPort serviceCatalogRepository;

    private ServiceCatalogService serviceCatalogService;

    @BeforeEach
    void setUp() {
        serviceCatalogService = new ServiceCatalogService(serviceCatalogRepository);
    }

    private ServiceCatalog createSampleService() {
        return ServiceCatalog.create(
                "SRV-OIL-01",
                "Oil Change",
                "Engine oil change",
                Money.of(new BigDecimal("150.00")),
                30L
        );
    }

    @Nested
    @DisplayName("createService()")
    class CreateServiceTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should successfully create a new service catalog item with 5 parameters")
            void shouldCreateServiceCatalogWithFiveParams() {
                // Arrange
                given(serviceCatalogRepository.existsByCode("SRV-OIL-01")).willReturn(false);
                given(serviceCatalogRepository.save(any(ServiceCatalog.class))).willAnswer(inv -> inv.getArgument(0));

                // Act
                ServiceCatalog result = serviceCatalogService.createService(
                        "SRV-OIL-01",
                        "Oil Change",
                        "Engine oil change",
                        Money.of(new BigDecimal("150.00")),
                        30L
                );

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getCode()).isEqualTo("SRV-OIL-01");
                assertThat(result.getName()).isEqualTo("Oil Change");
                assertThat(result.getEstimatedMinutes()).isEqualTo(30L);
                verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
            }

            @Test
            @DisplayName("Should successfully create a service catalog item with 4 parameters defaulting estimatedMinutes to 0L")
            void shouldCreateServiceCatalogWithFourParams() {
                // Arrange
                given(serviceCatalogRepository.existsByCode("SRV-ALN-01")).willReturn(false);
                given(serviceCatalogRepository.save(any(ServiceCatalog.class))).willAnswer(inv -> inv.getArgument(0));

                // Act
                ServiceCatalog result = serviceCatalogService.createService(
                        "SRV-ALN-01",
                        "Wheel Alignment",
                        "Wheel balancing and alignment",
                        Money.of(new BigDecimal("120.00"))
                );

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getCode()).isEqualTo("SRV-ALN-01");
                assertThat(result.getEstimatedMinutes()).isZero();
                verify(serviceCatalogRepository).save(any(ServiceCatalog.class));
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw AlreadyExistsException when service code already exists")
            void shouldThrowWhenCodeExists() {
                // Arrange
                given(serviceCatalogRepository.existsByCode("SRV-OIL-01")).willReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> serviceCatalogService.createService(
                        "SRV-OIL-01",
                        "Oil Change",
                        "Engine oil change",
                        Money.of(new BigDecimal("150.00")),
                        30L
                )).isInstanceOf(AlreadyExistsException.class)
                        .hasMessageContaining("Service with code SRV-OIL-01 already exists");
                verify(serviceCatalogRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("getById(), getByCode() and listAll()")
    class RetrievalTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should retrieve service catalog item by ID")
            void shouldRetrieveById() {
                // Arrange
                UUID id = UUID.randomUUID();
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.of(service));

                // Act
                ServiceCatalog found = serviceCatalogService.getById(id);

                // Assert
                assertThat(found).isSameAs(service);
                verify(serviceCatalogRepository).findById(id);
            }

            @Test
            @DisplayName("Should retrieve service catalog item by code")
            void shouldRetrieveByCode() {
                // Arrange
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findByCode("SRV-OIL-01")).willReturn(Optional.of(service));

                // Act
                ServiceCatalog found = serviceCatalogService.getByCode("SRV-OIL-01");

                // Assert
                assertThat(found).isSameAs(service);
                verify(serviceCatalogRepository).findByCode("SRV-OIL-01");
            }

            @Test
            @DisplayName("Should list all service catalog items")
            void shouldListAllServices() {
                // Arrange
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findAll()).willReturn(List.of(service));

                // Act
                List<ServiceCatalog> results = serviceCatalogService.listAll();

                // Assert
                assertThat(results).containsExactly(service);
                verify(serviceCatalogRepository).findAll();
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw ResourceNotFoundException when service not found by ID")
            void shouldThrowWhenIdNotFound() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> serviceCatalogService.getById(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining(id.toString());
            }

            @Test
            @DisplayName("Should throw ResourceNotFoundException when service not found by code")
            void shouldThrowWhenCodeNotFound() {
                // Arrange
                given(serviceCatalogRepository.findByCode("UNKNOWN")).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> serviceCatalogService.getByCode("UNKNOWN"))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining("UNKNOWN");
            }
        }
    }

    @Nested
    @DisplayName("Update Methods")
    class UpdateTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should update service details and price via updateService()")
            void shouldUpdateServiceDetailsAndPrice() {
                // Arrange
                UUID id = UUID.randomUUID();
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.of(service));
                given(serviceCatalogRepository.save(service)).willReturn(service);

                // Act
                ServiceCatalog updated = serviceCatalogService.updateService(
                        id,
                        "Premium Oil Change",
                        "Full synthetic oil change",
                        Money.of(new BigDecimal("200.00")),
                        45L
                );

                // Assert
                assertThat(updated.getName()).isEqualTo("Premium Oil Change");
                assertThat(updated.getDescription()).isEqualTo("Full synthetic oil change");
                assertThat(updated.getPrice()).isEqualTo(Money.of(new BigDecimal("200.00")));
                assertThat(updated.getEstimatedMinutes()).isEqualTo(45L);
                verify(serviceCatalogRepository).save(service);
            }

            @Test
            @DisplayName("Should update price via updatePrice()")
            void shouldUpdatePrice() {
                // Arrange
                UUID id = UUID.randomUUID();
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.of(service));
                given(serviceCatalogRepository.save(service)).willReturn(service);

                // Act
                ServiceCatalog updated = serviceCatalogService.updatePrice(id, Money.of(new BigDecimal("180.00")));

                // Assert
                assertThat(updated.getPrice()).isEqualTo(Money.of(new BigDecimal("180.00")));
                verify(serviceCatalogRepository).save(service);
            }

            @Test
            @DisplayName("Should update details via updateDetails(id, name, description)")
            void shouldUpdateDetailsWithoutMinutes() {
                // Arrange
                UUID id = UUID.randomUUID();
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.of(service));
                given(serviceCatalogRepository.save(service)).willReturn(service);

                // Act
                ServiceCatalog updated = serviceCatalogService.updateDetails(id, "Updated Name", "Updated Desc");

                // Assert
                assertThat(updated.getName()).isEqualTo("Updated Name");
                assertThat(updated.getDescription()).isEqualTo("Updated Desc");
                verify(serviceCatalogRepository).save(service);
            }

            @Test
            @DisplayName("Should update details via updateDetails(id, name, description, estimatedMinutes)")
            void shouldUpdateDetailsWithMinutes() {
                // Arrange
                UUID id = UUID.randomUUID();
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.of(service));
                given(serviceCatalogRepository.save(service)).willReturn(service);

                // Act
                ServiceCatalog updated = serviceCatalogService.updateDetails(id, "Updated Name 2", "Updated Desc 2", 60L);

                // Assert
                assertThat(updated.getName()).isEqualTo("Updated Name 2");
                assertThat(updated.getEstimatedMinutes()).isEqualTo(60L);
                verify(serviceCatalogRepository).save(service);
            }
        }
    }

    @Nested
    @DisplayName("deleteService()")
    class DeleteServiceTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should delete service when it exists")
            void shouldDeleteServiceWhenExists() {
                // Arrange
                UUID id = UUID.randomUUID();
                ServiceCatalog service = createSampleService();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.of(service));

                // Act
                serviceCatalogService.deleteService(id);

                // Assert
                verify(serviceCatalogRepository).deleteById(id);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw ResourceNotFoundException when deleting non-existent service")
            void shouldThrowWhenDeletingNonExistent() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(serviceCatalogRepository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> serviceCatalogService.deleteService(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining(id.toString());
                verify(serviceCatalogRepository, never()).deleteById(any());
            }
        }
    }
}
