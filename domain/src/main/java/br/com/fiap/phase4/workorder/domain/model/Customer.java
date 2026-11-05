package br.com.fiap.phase4.workorder.domain.model;

import br.com.fiap.phase4.commons.domain.vo.CpfOrCnpj;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, unique = true, length = 14)
    private String document;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public Customer(UUID id, String name, String email, String document, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = Objects.requireNonNull(name, "Customer name is required");
        this.email = Objects.requireNonNull(email, "Customer email is required");
        this.document = Objects.requireNonNull(document, "Customer document is required");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Customer create(String name, String email, String document) {
        return new Customer(UUID.randomUUID(), name, email, new CpfOrCnpj(document).value(), Instant.now(), Instant.now());
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.setCustomer(this);
        this.updatedAt = Instant.now();
    }

    public void update(String name, String email) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (email != null && !email.isBlank()) {
            this.email = email;
        }
        this.updatedAt = Instant.now();
    }

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }
}
