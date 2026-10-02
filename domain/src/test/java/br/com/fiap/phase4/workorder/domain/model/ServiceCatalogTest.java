package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ServiceCatalog Domain Model Unit Tests")
class ServiceCatalogTest {

    @Test
    @DisplayName("Should successfully create service catalog item")
    void shouldCreateServiceCatalogSuccessfully() {
        ServiceCatalog service = ServiceCatalog.create(
                "SRV-OIL-01",
                "Oil and Filter Change",
                "Engine oil and filter change",
                Money.of(new BigDecimal("150.00")),
                45L
        );

        assertThat(service.getId()).isNotNull();
        assertThat(service.getCode()).isEqualTo("SRV-OIL-01");
        assertThat(service.getName()).isEqualTo("Oil and Filter Change");
        assertThat(service.getPrice().amount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(service.getEstimatedMinutes()).isEqualTo(45L);
    }

    @Test
    @DisplayName("Should fail when code is blank")
    void shouldFailWhenCodeIsBlank() {
        assertThatThrownBy(() -> ServiceCatalog.create(
                "  ",
                "Oil Change",
                "Desc",
                Money.of(new BigDecimal("150.00")),
                30L
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("code cannot be blank");
    }

    @Test
    @DisplayName("Should fail when price is negative")
    void shouldFailWhenPriceIsNegative() {
        assertThatThrownBy(() -> ServiceCatalog.create(
                "SRV-OIL-01",
                "Oil Change",
                "Desc",
                Money.of(new BigDecimal("-50.00")),
                30L
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("Should successfully update service price")
    void shouldUpdateServicePriceSuccessfully() {
        ServiceCatalog service = ServiceCatalog.create(
                "SRV-OIL-01",
                "Oil Change",
                "Desc",
                Money.of(new BigDecimal("150.00")),
                30L
        );

        service.updatePrice(Money.of(new BigDecimal("180.00")));
        assertThat(service.getPrice().amount()).isEqualByComparingTo(new BigDecimal("180.00"));
    }
}
