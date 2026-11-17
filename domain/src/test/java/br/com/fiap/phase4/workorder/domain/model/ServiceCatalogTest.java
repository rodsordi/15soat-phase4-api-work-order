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

@DisplayName("ServiceCatalog Domain Model Unit Tests")
class ServiceCatalogTest {

    @Nested
    @DisplayName("Creation and Invariant Tests")
    class CreationTests {

        @Test
        @DisplayName("Should successfully create service catalog item with 5 parameters")
        void shouldCreateSuccessfullyWithFiveParams() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-OIL-01",
                    "Oil Change",
                    "Engine oil change",
                    Money.of(new BigDecimal("150.00")),
                    30L
            );

            assertThat(service.getId()).isNotNull();
            assertThat(service.getCode()).isEqualTo("SRV-OIL-01");
            assertThat(service.getName()).isEqualTo("Oil Change");
            assertThat(service.getDescription()).isEqualTo("Engine oil change");
            assertThat(service.getPrice().amount()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(service.getEstimatedMinutes()).isEqualTo(30L);
            assertThat(service.getCreatedAt()).isNotNull();
            assertThat(service.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create service catalog item with 4 parameters defaulting estimatedMinutes to 0L")
        void shouldCreateSuccessfullyWithFourParams() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-ALN-01",
                    "Wheel Alignment",
                    "Wheel balancing",
                    Money.of(new BigDecimal("120.00"))
            );

            assertThat(service.getCode()).isEqualTo("SRV-ALN-01");
            assertThat(service.getEstimatedMinutes()).isZero();
        }

        @Test
        @DisplayName("Should instantiate service catalog with full constructor")
        void shouldCreateWithFullConstructor() {
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ServiceCatalog service = new ServiceCatalog(
                    id, "SRV-BRK-01", "Brake Overhaul", "Full brake overhaul",
                    Money.of(new BigDecimal("300.00")), 90L, now, now
            );

            assertThat(service.getId()).isEqualTo(id);
            assertThat(service.getEstimatedMinutes()).isEqualTo(90L);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail when code is blank")
        void shouldFailWhenCodeIsBlank(String blankCode) {
            assertThatThrownBy(() -> ServiceCatalog.create(
                    blankCode, "Oil Change", "Desc", Money.of(new BigDecimal("100.00"))
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Service code cannot be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Should fail when name is blank")
        void shouldFailWhenNameIsBlank(String blankName) {
            assertThatThrownBy(() -> ServiceCatalog.create(
                    "SRV-01", blankName, "Desc", Money.of(new BigDecimal("100.00"))
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Service name cannot be blank");
        }

        @Test
        @DisplayName("Should fail when price is negative")
        void shouldFailWhenPriceIsNegative() {
            assertThatThrownBy(() -> ServiceCatalog.create(
                    "SRV-01", "Oil Change", "Desc", Money.of(new BigDecimal("-10.00"))
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Service price cannot be negative");
        }

        @Test
        @DisplayName("Should fail when estimated minutes is negative")
        void shouldFailWhenEstimatedMinutesIsNegative() {
            assertThatThrownBy(() -> ServiceCatalog.create(
                    "SRV-01", "Oil Change", "Desc", Money.of(new BigDecimal("100.00")), -5L
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Estimated duration cannot be negative");
        }
    }

    @Nested
    @DisplayName("Update Methods Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should successfully update price")
        void shouldUpdatePriceSuccessfully() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-OIL-01", "Oil Change", "Desc", Money.of(new BigDecimal("150.00")), 30L
            );

            service.updatePrice(Money.of(new BigDecimal("180.00")));
            assertThat(service.getPrice().amount()).isEqualByComparingTo(new BigDecimal("180.00"));
        }

        @Test
        @DisplayName("Should fail when updating to negative price")
        void shouldFailWhenUpdatingToNegativePrice() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-OIL-01", "Oil Change", "Desc", Money.of(new BigDecimal("150.00")), 30L
            );

            assertThatThrownBy(() -> service.updatePrice(Money.of(new BigDecimal("-1.00"))))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Service price cannot be negative");
        }

        @Test
        @DisplayName("Should update estimated minutes")
        void shouldUpdateEstimatedMinutes() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-OIL-01", "Oil Change", "Desc", Money.of(new BigDecimal("150.00")), 30L
            );

            service.updateEstimatedMinutes(45L);
            assertThat(service.getEstimatedMinutes()).isEqualTo(45L);

            service.updateEstimatedMinutes(-10L); // Negative should be ignored
            assertThat(service.getEstimatedMinutes()).isEqualTo(45L);
        }

        @Test
        @DisplayName("Should update details with name and description")
        void shouldUpdateDetails() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-OIL-01", "Oil Change", "Desc", Money.of(new BigDecimal("150.00")), 30L
            );

            service.updateDetails("Full Oil Change", "Complete engine flush and oil change");
            assertThat(service.getName()).isEqualTo("Full Oil Change");
            assertThat(service.getDescription()).isEqualTo("Complete engine flush and oil change");
        }

        @Test
        @DisplayName("Should update details with name, description and estimatedMinutes")
        void shouldUpdateDetailsWithMinutes() {
            ServiceCatalog service = ServiceCatalog.create(
                    "SRV-OIL-01", "Oil Change", "Desc", Money.of(new BigDecimal("150.00")), 30L
            );

            service.updateDetails("Full Oil Change", "Complete oil change", 60L);
            assertThat(service.getName()).isEqualTo("Full Oil Change");
            assertThat(service.getEstimatedMinutes()).isEqualTo(60L);
        }
    }
}
