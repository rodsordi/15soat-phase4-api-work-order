package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Material Domain Model Unit Tests")
class MaterialTest {

    @Test
    @DisplayName("Should successfully create material with valid attributes")
    void shouldCreateMaterialSuccessfully() {
        Material material = Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Ceramic front brake pads",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                20,
                5
        );

        assertThat(material.getId()).isNotNull();
        assertThat(material.getSku()).isEqualTo("PART-BRK-01");
        assertThat(material.getName()).isEqualTo("Front Brake Pads");
        assertThat(material.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("95.00"));
        assertThat(material.getStockQuantity()).isEqualTo(20);
        assertThat(material.getMinStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should fail when creating material with blank SKU")
    void shouldFailWhenSkuIsBlank() {
        assertThatThrownBy(() -> Material.create(
                "   ",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                10,
                2
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("SKU cannot be blank");
    }

    @Test
    @DisplayName("Should fail when unit price is negative")
    void shouldFailWhenUnitPriceIsNegative() {
        assertThatThrownBy(() -> Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("-10.00")),
                10,
                2
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Should successfully update price")
    void shouldUpdatePriceSuccessfully() {
        Material material = Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                10,
                2
        );

        material.updatePrice(Money.of(new BigDecimal("105.00")), Money.of(new BigDecimal("50.00")));

        assertThat(material.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("105.00"));
        assertThat(material.getUnitCost().amount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should successfully adjust stock")
    void shouldAdjustStockSuccessfully() {
        Material material = Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                10,
                2
        );

        material.adjustStock(-3);
        assertThat(material.getStockQuantity()).isEqualTo(7);

        material.adjustStock(5);
        assertThat(material.getStockQuantity()).isEqualTo(12);
    }

    @Test
    @DisplayName("Should throw exception when stock adjustment goes below zero")
    void shouldFailWhenStockGoesBelowZero() {
        Material material = Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                2,
                1
        );

        assertThatThrownBy(() -> material.adjustStock(-5))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Stock cannot drop below zero");
    }

    @Test
    @DisplayName("Should successfully reserve, release and conclude material stock")
    void shouldManageReservationLifecycleSuccessfully() {
        Material material = Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                10,
                2
        );

        material.reserveQuantity(4);
        assertThat(material.getReservedQuantity()).isEqualTo(4);
        assertThat(material.getStockQuantity()).isEqualTo(10);

        material.releaseReservedQuantity(1);
        assertThat(material.getReservedQuantity()).isEqualTo(3);

        material.concludeReservedQuantity(2);
        assertThat(material.getReservedQuantity()).isEqualTo(1);
        assertThat(material.getStockQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should fail when attempting to reserve more than available stock")
    void shouldFailWhenReservingMoreThanAvailable() {
        Material material = Material.create(
                "PART-BRK-01",
                "Front Brake Pads",
                "Description",
                Money.of(new BigDecimal("45.00")),
                Money.of(new BigDecimal("95.00")),
                5,
                1
        );

        material.reserveQuantity(3);
        assertThatThrownBy(() -> material.reserveQuantity(3))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Cannot reserve 3 units");
    }
}
