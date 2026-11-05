package br.com.fiap.phase4.workorder.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "work_order_status_history")
public class WorkOrderStatusHistory {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private WorkOrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private WorkOrderStatus toStatus;

    @Column(length = 500)
    private String reason;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    public WorkOrderStatusHistory(UUID id, WorkOrder workOrder, WorkOrderStatus fromStatus,
                                  WorkOrderStatus toStatus, String reason,
                                  Instant changedAt, String changedBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.workOrder = Objects.requireNonNull(workOrder, "WorkOrder is required");
        this.fromStatus = fromStatus;
        this.toStatus = Objects.requireNonNull(toStatus, "Target status is required");
        this.reason = reason;
        this.changedAt = changedAt != null ? changedAt : Instant.now();
        this.changedBy = changedBy;
    }

    public static WorkOrderStatusHistory record(WorkOrder workOrder, WorkOrderStatus from,
                                                WorkOrderStatus to, String reason, String changedBy) {
        return new WorkOrderStatusHistory(UUID.randomUUID(), workOrder, from, to, reason, Instant.now(), changedBy);
    }
}
