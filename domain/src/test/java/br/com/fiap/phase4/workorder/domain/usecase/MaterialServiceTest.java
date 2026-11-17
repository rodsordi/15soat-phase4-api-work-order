package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.AlreadyExistsException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
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
@DisplayName("MaterialService Unit Tests")
class MaterialServiceTest {

    @Mock
    private MaterialRepositoryPort materialRepository;

    private MaterialService materialService;

    @BeforeEach
    void setUp() {
        materialService = new MaterialService(materialRepository);
    }

    private Material createSampleMaterial() {
        return Material.create(
                "PART-BRK-01",
                "Brake Pads",
                "Front brake pads",
                Money.of(new BigDecimal("40.00")),
                Money.of(new BigDecimal("90.00")),
                15,
                3
        );
    }

    @Nested
    @DisplayName("createMaterial()")
    class CreateMaterialTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should successfully create a new material when SKU is unique")
            void shouldCreateMaterialWhenSkuIsUnique() {
                // Arrange
                given(materialRepository.existsBySku("PART-BRK-01")).willReturn(false);
                given(materialRepository.save(any(Material.class))).willAnswer(inv -> inv.getArgument(0));

                // Act
                Material result = materialService.createMaterial(
                        "PART-BRK-01",
                        "Brake Pads",
                        "Front brake pads",
                        Money.of(new BigDecimal("40.00")),
                        Money.of(new BigDecimal("90.00")),
                        15,
                        3
                );

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getSku()).isEqualTo("PART-BRK-01");
                assertThat(result.getName()).isEqualTo("Brake Pads");
                verify(materialRepository).save(any(Material.class));
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw AlreadyExistsException when SKU already exists")
            void shouldThrowWhenSkuExists() {
                // Arrange
                given(materialRepository.existsBySku("PART-BRK-01")).willReturn(true);

                // Act & Assert
                assertThatThrownBy(() -> materialService.createMaterial(
                        "PART-BRK-01",
                        "Brake Pads",
                        "Front brake pads",
                        Money.of(new BigDecimal("40.00")),
                        Money.of(new BigDecimal("90.00")),
                        15,
                        3
                )).isInstanceOf(AlreadyExistsException.class)
                        .hasMessageContaining("Material with SKU PART-BRK-01 already exists");
                verify(materialRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("getById() and getBySku()")
    class RetrievalTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should retrieve material by ID")
            void shouldRetrieveById() {
                // Arrange
                UUID id = UUID.randomUUID();
                Material material = createSampleMaterial();
                given(materialRepository.findById(id)).willReturn(Optional.of(material));

                // Act
                Material found = materialService.getById(id);

                // Assert
                assertThat(found).isSameAs(material);
                verify(materialRepository).findById(id);
            }

            @Test
            @DisplayName("Should retrieve material by SKU")
            void shouldRetrieveBySku() {
                // Arrange
                Material material = createSampleMaterial();
                given(materialRepository.findBySku("PART-BRK-01")).willReturn(Optional.of(material));

                // Act
                Material found = materialService.getBySku("PART-BRK-01");

                // Assert
                assertThat(found).isSameAs(material);
                verify(materialRepository).findBySku("PART-BRK-01");
            }

            @Test
            @DisplayName("Should list all materials")
            void shouldListAllMaterials() {
                // Arrange
                Material material = createSampleMaterial();
                given(materialRepository.findAll()).willReturn(List.of(material));

                // Act
                List<Material> results = materialService.listAll();

                // Assert
                assertThat(results).containsExactly(material);
                verify(materialRepository).findAll();
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw ResourceNotFoundException when material not found by ID")
            void shouldThrowWhenIdNotFound() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(materialRepository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> materialService.getById(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining(id.toString());
            }

            @Test
            @DisplayName("Should throw ResourceNotFoundException when material not found by SKU")
            void shouldThrowWhenSkuNotFound() {
                // Arrange
                given(materialRepository.findBySku("UNKNOWN")).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> materialService.getBySku("UNKNOWN"))
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
            @DisplayName("Should update full material details and prices")
            void shouldUpdateFullMaterial() {
                // Arrange
                UUID id = UUID.randomUUID();
                Material material = createSampleMaterial();
                given(materialRepository.findById(id)).willReturn(Optional.of(material));
                given(materialRepository.save(material)).willReturn(material);

                // Act
                Material updated = materialService.updateMaterial(
                        id,
                        "Premium Brake Pads",
                        "Ceramic pads",
                        Money.of(new BigDecimal("50.00")),
                        Money.of(new BigDecimal("110.00")),
                        5
                );

                // Assert
                assertThat(updated.getName()).isEqualTo("Premium Brake Pads");
                assertThat(updated.getDescription()).isEqualTo("Ceramic pads");
                assertThat(updated.getUnitCost()).isEqualTo(Money.of(new BigDecimal("50.00")));
                assertThat(updated.getUnitPrice()).isEqualTo(Money.of(new BigDecimal("110.00")));
                assertThat(updated.getMinStock()).isEqualTo(5);
                verify(materialRepository).save(material);
            }

            @Test
            @DisplayName("Should update price and cost via updatePrice()")
            void shouldUpdatePrice() {
                // Arrange
                UUID id = UUID.randomUUID();
                Material material = createSampleMaterial();
                given(materialRepository.findById(id)).willReturn(Optional.of(material));
                given(materialRepository.save(material)).willReturn(material);

                // Act
                Material updated = materialService.updatePrice(id, Money.of(new BigDecimal("120.00")), Money.of(new BigDecimal("55.00")));

                // Assert
                assertThat(updated.getUnitPrice()).isEqualTo(Money.of(new BigDecimal("120.00")));
                assertThat(updated.getUnitCost()).isEqualTo(Money.of(new BigDecimal("55.00")));
                verify(materialRepository).save(material);
            }

            @Test
            @DisplayName("Should update stock delta via updateStock()")
            void shouldUpdateStock() {
                // Arrange
                UUID id = UUID.randomUUID();
                Material material = createSampleMaterial();
                given(materialRepository.findById(id)).willReturn(Optional.of(material));
                given(materialRepository.save(material)).willReturn(material);

                // Act
                Material updated = materialService.updateStock(id, 5);

                // Assert
                assertThat(updated.getStockQuantity()).isEqualTo(20);
                verify(materialRepository).save(material);
            }

            @Test
            @DisplayName("Should update details via updateDetails()")
            void shouldUpdateDetails() {
                // Arrange
                UUID id = UUID.randomUUID();
                Material material = createSampleMaterial();
                given(materialRepository.findById(id)).willReturn(Optional.of(material));
                given(materialRepository.save(material)).willReturn(material);

                // Act
                Material updated = materialService.updateDetails(id, "Updated Name", "Updated Desc", 10);

                // Assert
                assertThat(updated.getName()).isEqualTo("Updated Name");
                assertThat(updated.getMinStock()).isEqualTo(10);
                verify(materialRepository).save(material);
            }
        }
    }

    @Nested
    @DisplayName("deleteMaterial()")
    class DeleteMaterialTests {

        @Nested
        @DisplayName("Success Scenarios")
        class SuccessScenarios {

            @Test
            @DisplayName("Should delete material when it exists")
            void shouldDeleteMaterialWhenExists() {
                // Arrange
                UUID id = UUID.randomUUID();
                Material material = createSampleMaterial();
                given(materialRepository.findById(id)).willReturn(Optional.of(material));

                // Act
                materialService.deleteMaterial(id);

                // Assert
                verify(materialRepository).deleteById(id);
            }
        }

        @Nested
        @DisplayName("Failure Scenarios")
        class FailureScenarios {

            @Test
            @DisplayName("Should throw ResourceNotFoundException when deleting non-existent material")
            void shouldThrowWhenDeletingNonExistent() {
                // Arrange
                UUID id = UUID.randomUUID();
                given(materialRepository.findById(id)).willReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> materialService.deleteMaterial(id))
                        .isInstanceOf(ResourceNotFoundException.class)
                        .hasMessageContaining(id.toString());
                verify(materialRepository, never()).deleteById(any());
            }
        }
    }
}
