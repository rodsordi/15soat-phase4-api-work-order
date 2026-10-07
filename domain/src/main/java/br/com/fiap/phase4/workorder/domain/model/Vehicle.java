package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.vo.LicensePlate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "license_plate", nullable = false, unique = true, length = 10)
    private String licensePlate;

    @Column(nullable = false, length = 100)
    private String make;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public Vehicle(UUID id, Customer customer, String licensePlate, String make, String model, Integer manufactureYear,
                   Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.customer = Objects.requireNonNull(customer, "Customer is required");
        this.licensePlate = Objects.requireNonNull(licensePlate, "License plate is required");
        this.make = Objects.requireNonNull(make, "Vehicle make is required");
        this.model = Objects.requireNonNull(model, "Vehicle model is required");
        this.manufactureYear = Objects.requireNonNull(manufactureYear, "Manufacture year is required");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Vehicle create(Customer customer, String licensePlate, String make, String model, Integer manufactureYear) {
        return new Vehicle(UUID.randomUUID(), customer, new LicensePlate(licensePlate).value(), make, model, manufactureYear,
                Instant.now(), Instant.now());
    }

    public void update(String make, String model, Integer manufactureYear) {
        if (make != null && !make.isBlank()) {
            this.make = make;
        }
        if (model != null && !model.isBlank()) {
            this.model = model;
        }
        if (manufactureYear != null) {
            this.manufactureYear = manufactureYear;
        }
        this.updatedAt = Instant.now();
    }
}
