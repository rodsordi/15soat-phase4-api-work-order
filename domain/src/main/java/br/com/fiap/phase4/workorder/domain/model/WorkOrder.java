package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import br.com.fiap.phase4.commons.domain.vo.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "work_order")
public class WorkOrder {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Getter(AccessLevel.NONE)
    @Column(name = "customer_document", nullable = false, length = 14)
    private String customerDocument;

    @Getter(AccessLevel.NONE)
    @Column(name = "license_plate", nullable = false, length = 10)
    private String licensePlate;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WorkOrderStatus status;

    @Getter(AccessLevel.NONE)
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkOrderServiceItem> services = new ArrayList<>();

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkOrderMaterialItem> materials = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public WorkOrder(UUID id, Customer customer, Vehicle vehicle, String customerDocument, String licensePlate,
                     String description, WorkOrderStatus status, BigDecimal totalAmount,
                     List<WorkOrderServiceItem> services, List<WorkOrderMaterialItem> materials,
                     Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.customer = customer;
        this.vehicle = vehicle;
        this.customerDocument = customerDocument != null ? customerDocument : (customer != null ? customer.getDocument() : "");
        this.licensePlate = licensePlate != null ? licensePlate : (vehicle != null ? vehicle.getLicensePlate() : "");
        this.description = Objects.requireNonNull(description, "Description is required");
        this.status = status != null ? status : WorkOrderStatus.RECEIVED;
        this.services = services != null ? new ArrayList<>(services) : new ArrayList<>();
        this.materials = materials != null ? new ArrayList<>(materials) : new ArrayList<>();
        this.totalAmount = totalAmount != null ? totalAmount : calculateTotalAmount().amount();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public WorkOrder(UUID id, CpfOrCnpj customerDocument, LicensePlate licensePlate, String description,
                     WorkOrderStatus status, Money totalAmount, Instant createdAt, Instant updatedAt) {
        this(id, null, null, customerDocument.value(), licensePlate.value(), description, status,
                totalAmount != null ? totalAmount.amount() : BigDecimal.ZERO, List.of(), List.of(), createdAt, updatedAt);
    }

    public static WorkOrder create(Customer customer, Vehicle vehicle, String description) {
        return new WorkOrder(UUID.randomUUID(), customer, vehicle, customer.getDocument(), vehicle.getLicensePlate(),
                description, WorkOrderStatus.RECEIVED, BigDecimal.ZERO, List.of(), List.of(), Instant.now(), Instant.now());
    }

    public static WorkOrder create(CpfOrCnpj customerDocument, LicensePlate licensePlate, String description) {
        return new WorkOrder(UUID.randomUUID(), null, null, customerDocument.value(), licensePlate.value(),
                description, WorkOrderStatus.RECEIVED, BigDecimal.ZERO, List.of(), List.of(), Instant.now(), Instant.now());
    }

    public void approve() {
        if (this.status == WorkOrderStatus.CANCELED || this.status == WorkOrderStatus.COMPLETED) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "Cannot approve a work order in status: " + this.status);
        }
        this.status = WorkOrderStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == WorkOrderStatus.COMPLETED) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "Cannot cancel an already completed work order");
        }
        this.status = WorkOrderStatus.CANCELED;
        this.updatedAt = Instant.now();
    }

    public void rejectPayment(String reason) {
        this.status = WorkOrderStatus.PAYMENT_REJECTED;
        this.updatedAt = Instant.now();
    }

    public void complete() {
        this.status = WorkOrderStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void addService(UUID serviceId, Money unitPrice, int quantity) {
        WorkOrderServiceItem item = WorkOrderServiceItem.of(this, serviceId, unitPrice, quantity);
        this.services.add(item);
        this.totalAmount = calculateTotalAmount().amount();
        this.updatedAt = Instant.now();
    }

    public void addMaterial(UUID materialId, Money unitPrice, int quantity) {
        WorkOrderMaterialItem item = WorkOrderMaterialItem.of(this, materialId, unitPrice, quantity);
        this.materials.add(item);
        this.totalAmount = calculateTotalAmount().amount();
        this.updatedAt = Instant.now();
    }

    public Money calculateTotalAmount() {
        Money serviceTotal = services.stream()
                .map(WorkOrderServiceItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);
        Money materialTotal = materials.stream()
                .map(WorkOrderMaterialItem::getSubtotal)
                .reduce(Money.ZERO, Money::add);
        return serviceTotal.add(materialTotal);
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null").amount();
        this.updatedAt = Instant.now();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerDocument = customer.getDocument();
        }
        this.updatedAt = Instant.now();
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null) {
            this.licensePlate = vehicle.getLicensePlate();
        }
        this.updatedAt = Instant.now();
    }

    public CpfOrCnpj getCustomerDocument() {
        return new CpfOrCnpj(customerDocument);
    }

    public LicensePlate getLicensePlate() {
        return new LicensePlate(licensePlate);
    }

    public Money getTotalAmount() {
        return totalAmount != null ? Money.of(totalAmount) : Money.ZERO;
    }

    public List<WorkOrderServiceItem> getServices() {
        return Collections.unmodifiableList(services);
    }

    public List<WorkOrderMaterialItem> getMaterials() {
        return Collections.unmodifiableList(materials);
    }
}
