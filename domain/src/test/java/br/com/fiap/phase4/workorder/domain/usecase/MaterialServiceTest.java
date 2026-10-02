package br.com.fiap.phase4.workorder.domain.usecase;

import br.com.fiap.phase4.commons.domain.exception.AlreadyExistsException;
import br.com.fiap.phase4.commons.domain.exception.ResourceNotFoundException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import br.com.fiap.phase4.workorder.domain.model.Material;
import br.com.fiap.phase4.workorder.domain.port.out.MaterialRepositoryPort;
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
@DisplayName("MaterialService Use Case Unit Tests")
class MaterialServiceTest {

    @Mock
    private MaterialRepositoryPort materialRepository;

    private MaterialService materialService;

    @BeforeEach
    void setUp() {
        materialService = new MaterialService(materialRepository);
    }

    @Test
    @DisplayName("Should successfully create a new material when SKU is unique")
    void shouldCreateMaterialWhenSkuIsUnique() {
        when(materialRepository.existsBySku("PART-BRK-01")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.createMaterial(
                "PART-BRK-01",
                "Brake Pads",
                "Front brake pads",
                Money.of(new BigDecimal("40.00")),
                Money.of(new BigDecimal("90.00")),
                15,
                3
        );

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("PART-BRK-01");
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    @DisplayName("Should throw AlreadyExistsException when SKU already exists")
    void shouldThrowWhenSkuExists() {
        when(materialRepository.existsBySku("PART-BRK-01")).thenReturn(true);

        assertThatThrownBy(() -> materialService.createMaterial(
                "PART-BRK-01",
                "Brake Pads",
                "Front brake pads",
                Money.of(new BigDecimal("40.00")),
                Money.of(new BigDecimal("90.00")),
                15,
                3
        )).isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should retrieve material by SKU")
    void shouldRetrieveMaterialBySku() {
        Material material = Material.create(
                "PART-BRK-01",
                "Brake Pads",
                "Front brake pads",
                Money.of(new BigDecimal("40.00")),
                Money.of(new BigDecimal("90.00")),
                15,
                3
        );
        when(materialRepository.findBySku("PART-BRK-01")).thenReturn(Optional.of(material));

        Material found = materialService.getBySku("PART-BRK-01");
        assertThat(found.getSku()).isEqualTo("PART-BRK-01");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when material not found")
    void shouldThrowWhenMaterialNotFound() {
        UUID id = UUID.randomUUID();
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
