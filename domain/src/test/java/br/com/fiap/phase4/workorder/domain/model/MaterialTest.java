package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Material Domain Model Unit Tests")
class MaterialTest {

    @Nested
    @DisplayName("Creation and Invariant Tests")
    class CreationTests {

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
            assertThat(material.getDescription()).isEqualTo("Ceramic front brake pads");
            assertThat(material.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("95.00"));
            assertThat(material.getUnitCost().amount()).isEqualByComparingTo(new BigDecimal("45.00"));
            assertThat(material.getStockQuantity()).isEqualTo(20);
            assertThat(material.getReservedQuantity()).isZero();
            assertThat(material.getMinStock()).isEqualTo(5);
            assertThat(material.getAvailableQuantity()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should instantiate material using full 11-argument constructor")
        void shouldCreateMaterialWithFullConstructor() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            Material material = new Material(
                    id, "PART-ENG-02", "Timing Belt", "Rubber belt",
                    Money.of(new BigDecimal("30.00")), Money.of(new BigDecimal("80.00")),
                    10, 2, 3, now, now
            );

            assertThat(material.getId()).isEqualTo(id);
            assertThat(material.getReservedQuantity()).isEqualTo(2);
            assertThat(material.getAvailableQuantity()).isEqualTo(8);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail when creating material with blank SKU")
        void shouldFailWhenSkuIsBlank(String blankSku) {
            assertThatThrownBy(() -> Material.create(
                    blankSku,
                    "Front Brake Pads",
                    "Description",
                    Money.of(new BigDecimal("45.00")),
                    Money.of(new BigDecimal("95.00")),
                    10,
                    2
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("SKU cannot be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail when creating material with blank name")
        void shouldFailWhenNameIsBlank(String blankName) {
            assertThatThrownBy(() -> Material.create(
                    "PART-BRK-01",
                    blankName,
                    "Description",
                    Money.of(new BigDecimal("45.00")),
                    Money.of(new BigDecimal("95.00")),
                    10,
                    2
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Material name cannot be blank");
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
        @DisplayName("Should fail when initial stock is negative")
        void shouldFailWhenInitialStockIsNegative() {
            assertThatThrownBy(() -> Material.create(
                    "PART-BRK-01",
                    "Front Brake Pads",
                    "Description",
                    Money.of(new BigDecimal("45.00")),
                    Money.of(new BigDecimal("95.00")),
                    -5,
                    2
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Stock quantity cannot be negative");
        }

        @Test
        @DisplayName("Should fail when reserved quantity in full constructor is negative")
        void shouldFailWhenReservedQuantityIsNegative() {
            assertThatThrownBy(() -> new Material(
                    UUID.randomUUID(), "PART-01", "Pads", "Desc",
                    Money.of(new BigDecimal("10.00")), Money.of(new BigDecimal("20.00")),
                    10, -1, 2, Instant.now(), Instant.now()
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Reserved quantity cannot be negative");
        }
    }

    @Nested
    @DisplayName("Stock and Reservation Lifecycle Tests")
    class StockTests {

        @Test
        @DisplayName("Should successfully update price and cost")
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
        @DisplayName("Should fail when new price is negative")
        void shouldFailWhenNewPriceIsNegative() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 10, 2
            );

            assertThatThrownBy(() -> material.updatePrice(Money.of(new BigDecimal("-5.00")), null))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("New unit price cannot be negative");
        }

        @Test
        @DisplayName("Should successfully adjust stock up and down")
        void shouldAdjustStockSuccessfully() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 10, 2
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
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 2, 1
            );

            assertThatThrownBy(() -> material.adjustStock(-5))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Stock cannot drop below zero");
        }

        @Test
        @DisplayName("Should successfully reserve, release and conclude material stock")
        void shouldManageReservationLifecycleSuccessfully() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 10, 2
            );

            material.reserveQuantity(4);
            assertThat(material.getReservedQuantity()).isEqualTo(4);
            assertThat(material.getAvailableQuantity()).isEqualTo(6);

            material.releaseReservedQuantity(1);
            assertThat(material.getReservedQuantity()).isEqualTo(3);

            material.concludeReservedQuantity(2);
            assertThat(material.getReservedQuantity()).isEqualTo(1);
            assertThat(material.getStockQuantity()).isEqualTo(8);
        }

        @Test
        @DisplayName("Should fail when reserving non-positive quantity")
        void shouldFailWhenReservingNonPositive() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 5, 1
            );

            assertThatThrownBy(() -> material.reserveQuantity(0))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Quantity to reserve must be greater than zero");
        }

        @Test
        @DisplayName("Should fail when attempting to reserve more than available stock")
        void shouldFailWhenReservingMoreThanAvailable() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 5, 1
            );

            material.reserveQuantity(3);
            assertThatThrownBy(() -> material.reserveQuantity(3))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Cannot reserve 3 units");
        }

        @Test
        @DisplayName("Should fail when concluding more than currently reserved")
        void shouldFailWhenConcludingMoreThanReserved() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 5, 1
            );
            material.reserveQuantity(2);

            assertThatThrownBy(() -> material.concludeReservedQuantity(3))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Cannot conclude 3 units");
        }

        @Test
        @DisplayName("Should fail when releasing or concluding non-positive quantity")
        void shouldFailWhenReleaseOrConcludeNonPositive() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 5, 1
            );

            assertThatThrownBy(() -> material.releaseReservedQuantity(0))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Quantity to release must be greater than zero");

            assertThatThrownBy(() -> material.concludeReservedQuantity(-1))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Quantity to conclude must be greater than zero");
        }

        @Test
        @DisplayName("Should update material details via updateDetails()")
        void shouldUpdateDetails() {
            Material material = Material.create(
                    "PART-BRK-01", "Pads", "Desc",
                    Money.of(new BigDecimal("45.00")), Money.of(new BigDecimal("95.00")), 5, 1
            );

            material.updateDetails("New Pads", "New Description", 10);

            assertThat(material.getName()).isEqualTo("New Pads");
            assertThat(material.getDescription()).isEqualTo("New Description");
            assertThat(material.getMinStock()).isEqualTo(10);
        }
    }
}
