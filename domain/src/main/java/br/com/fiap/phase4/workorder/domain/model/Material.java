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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "material")
public class Material {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Getter(AccessLevel.NONE)
    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Getter(AccessLevel.NONE)
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public Material(UUID id, String sku, String name, String description,
                    Money unitCost, Money unitPrice, Integer stockQuantity, Integer reservedQuantity, Integer minStock,
                    Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.sku = Objects.requireNonNull(sku, "SKU is required").trim().toUpperCase();
        if (this.sku.isBlank()) {
            throw new DomainException("INVALID_SKU", "Material SKU cannot be blank");
        }
        this.name = Objects.requireNonNull(name, "Material name is required").trim();
        if (this.name.isBlank()) {
            throw new DomainException("INVALID_NAME", "Material name cannot be blank");
        }
        this.description = description;
        this.unitCost = unitCost != null ? unitCost.amount() : BigDecimal.ZERO;
        Objects.requireNonNull(unitPrice, "Unit price is required");
        if (unitPrice.isNegative()) {
            throw new DomainException("INVALID_PRICE", "Unit price cannot be negative");
        }
        this.unitPrice = unitPrice.amount();
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        if (this.stockQuantity < 0) {
            throw new DomainException("INVALID_STOCK", "Stock quantity cannot be negative");
        }
        this.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
        if (this.reservedQuantity < 0) {
            throw new DomainException("INVALID_STOCK", "Reserved quantity cannot be negative");
        }
        this.minStock = minStock != null ? minStock : 0;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public Material(UUID id, String sku, String name, String description,
                    Money unitCost, Money unitPrice, Integer stockQuantity, Integer minStock,
                    Instant createdAt, Instant updatedAt) {
        this(id, sku, name, description, unitCost, unitPrice, stockQuantity, 0, minStock, createdAt, updatedAt);
    }

    public static Material create(String sku, String name, String description,
                                  Money unitCost, Money unitPrice, Integer initialStock, Integer minStock) {
        return new Material(UUID.randomUUID(), sku, name, description, unitCost, unitPrice, initialStock, 0, minStock,
                Instant.now(), Instant.now());
    }

    public void updatePrice(Money newPrice, Money newCost) {
        Objects.requireNonNull(newPrice, "New unit price cannot be null");
        if (newPrice.isNegative()) {
            throw new DomainException("INVALID_PRICE", "New unit price cannot be negative");
        }
        this.unitPrice = newPrice.amount();
        if (newCost != null) {
            this.unitCost = newCost.amount();
        }
        this.updatedAt = Instant.now();
    }

    public void adjustStock(int quantityDelta) {
        int newStock = this.stockQuantity + quantityDelta;
        if (newStock < 0) {
            throw new DomainException("INSUFFICIENT_STOCK",
                    "Stock cannot drop below zero. Current: " + this.stockQuantity + ", delta: " + quantityDelta);
        }
        this.stockQuantity = newStock;
        this.updatedAt = Instant.now();
    }

    public void reserveQuantity(int quantityToReserve) {
        if (quantityToReserve <= 0) {
            throw new DomainException("INVALID_QUANTITY", "Quantity to reserve must be greater than zero");
        }
        int available = this.stockQuantity - this.reservedQuantity;
        if (quantityToReserve > available) {
            throw new DomainException("INSUFFICIENT_STOCK",
                    String.format("Cannot reserve %d units. Available: %d (in stock: %d, already reserved: %d)",
                            quantityToReserve, available, this.stockQuantity, this.reservedQuantity));
        }
        this.reservedQuantity += quantityToReserve;
        this.updatedAt = Instant.now();
    }

    public void releaseReservedQuantity(int quantityToRelease) {
        if (quantityToRelease <= 0) {
            throw new DomainException("INVALID_QUANTITY", "Quantity to release must be greater than zero");
        }
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantityToRelease);
        this.updatedAt = Instant.now();
    }

    public void concludeReservedQuantity(int quantityToConclude) {
        if (quantityToConclude <= 0) {
            throw new DomainException("INVALID_QUANTITY", "Quantity to conclude must be greater than zero");
        }
        if (quantityToConclude > this.reservedQuantity) {
            throw new DomainException("INSUFFICIENT_RESERVATION",
                    String.format("Cannot conclude %d units. Currently reserved: %d",
                            quantityToConclude, this.reservedQuantity));
        }
        this.reservedQuantity -= quantityToConclude;
        this.stockQuantity = Math.max(0, this.stockQuantity - quantityToConclude);
        this.updatedAt = Instant.now();
    }

    public void updateDetails(String name, String description, Integer minStock) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (minStock != null && minStock >= 0) {
            this.minStock = minStock;
        }
        this.updatedAt = Instant.now();
    }

    public Money getUnitCost() {
        return unitCost != null ? Money.of(unitCost) : Money.ZERO;
    }

    public Money getUnitPrice() {
        return unitPrice != null ? Money.of(unitPrice) : Money.ZERO;
    }
}
