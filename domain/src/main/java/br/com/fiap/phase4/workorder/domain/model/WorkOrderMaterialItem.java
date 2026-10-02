package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.vo.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "work_order_material")
public class WorkOrderMaterialItem {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Getter(AccessLevel.NONE)
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public WorkOrderMaterialItem(UUID id, WorkOrder workOrder, UUID materialId, BigDecimal unitPrice, Integer quantity, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.workOrder = workOrder;
        this.materialId = Objects.requireNonNull(materialId, "Material ID is required");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price is required");
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static WorkOrderMaterialItem of(WorkOrder workOrder, UUID materialId, Money unitPrice, int quantity) {
        return new WorkOrderMaterialItem(UUID.randomUUID(), workOrder, materialId, unitPrice.amount(), quantity, Instant.now());
    }

    public Money getUnitPrice() {
        return unitPrice != null ? Money.of(unitPrice) : Money.ZERO;
    }

    public Money getSubtotal() {
        return getUnitPrice().multiply(quantity);
    }
}
