package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.vo.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "service")
public class ServiceCatalog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Getter(AccessLevel.NONE)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "estimated_minutes", nullable = false)
    private Long estimatedMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public ServiceCatalog(UUID id, String code, String name, String description,
                          Money price, Long estimatedMinutes, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.code = Objects.requireNonNull(code, "Service code is required").trim().toUpperCase();
        if (this.code.isBlank()) {
            throw new DomainException("INVALID_CODE", "Service code cannot be blank");
        }
        this.name = Objects.requireNonNull(name, "Service name is required").trim();
        if (this.name.isBlank()) {
            throw new DomainException("INVALID_NAME", "Service name cannot be blank");
        }
        this.description = description;
        Objects.requireNonNull(price, "Service price is required");
        if (price.isNegative()) {
            throw new DomainException("INVALID_PRICE", "Service price cannot be negative");
        }
        this.price = price.amount();
        this.estimatedMinutes = estimatedMinutes != null ? estimatedMinutes : 0L;
        if (this.estimatedMinutes < 0) {
            throw new DomainException("INVALID_DURATION", "Estimated duration cannot be negative");
        }
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static ServiceCatalog create(String code, String name, String description,
                                        Money price, Long estimatedMinutes) {
        return new ServiceCatalog(UUID.randomUUID(), code, name, description, price, estimatedMinutes,
                Instant.now(), Instant.now());
    }

    public void updatePrice(Money newPrice) {
        Objects.requireNonNull(newPrice, "New price cannot be null");
        if (newPrice.isNegative()) {
            throw new DomainException("INVALID_PRICE", "Service price cannot be negative");
        }
        this.price = newPrice.amount();
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String name, String description, Long estimatedMinutes) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (estimatedMinutes != null && estimatedMinutes >= 0) {
            this.estimatedMinutes = estimatedMinutes;
        }
        this.updatedAt = Instant.now();
    }

    public Money getPrice() {
        return price != null ? Money.of(price) : Money.ZERO;
    }
}
