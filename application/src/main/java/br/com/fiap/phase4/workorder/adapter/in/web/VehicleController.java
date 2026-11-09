package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.commons.domain.exception.DomainException;
import br.com.fiap.phase4.workorder.adapter.in.web.api.VehicleApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateVehicleRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PagedVehicleResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.PatchVehicleRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.VehicleCustomerItemResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.VehicleDetailedResponse;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.VehicleSummaryResponse;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.in.CreateVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.DeleteVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.UpdateVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VehicleController implements VehicleApi {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public ResponseEntity<VehicleDetailedResponse> createVehicle(CreateVehicleRequest request) {
        String make = request.getMake() != null ? request.getMake() : request.getBrand();
        Integer year = request.getManufactureYear() != null ? request.getManufactureYear() : request.getYear();
        log.info("Received request to create vehicle with license plate: {}", request.getLicensePlate());
        Vehicle vehicle = createVehicleUseCase.createVehicle(
                request.getCustomerDocument(),
                request.getLicensePlate(),
                make,
                request.getModel(),
                year
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailedResponse(vehicle));
    }

    @Override
    public ResponseEntity<VehicleDetailedResponse> getVehicleById(UUID id) {
        log.info("Received request to get vehicle by ID: {}", id);
        Vehicle vehicle = getVehicleUseCase.getById(id);
        return ResponseEntity.ok(toDetailedResponse(vehicle));
    }

    @Override
    public ResponseEntity<PagedVehicleResponse> listVehicles(
            String licensePlate, String customerDocument, OffsetDateTime createdFrom,
            OffsetDateTime createdTo, Integer page, Integer size, List<String> sort) {
        log.info("Received request to list vehicles (licensePlate: {}, customerDocument: {})", licensePlate, customerDocument);
        List<Vehicle> list;
        if (licensePlate != null && !licensePlate.isBlank()) {
            try {
                Vehicle v = getVehicleUseCase.getByLicensePlate(licensePlate);
                list = List.of(v);
            } catch (DomainException e) {
                list = List.of();
            }
        } else {
            list = vehicleRepositoryPort.findAll();
            if (customerDocument != null && !customerDocument.isBlank()) {
                list = list.stream()
                        .filter(v -> v.getCustomer() != null && customerDocument.equals(v.getCustomer().getDocument()))
                        .toList();
            }
        }

        PagedVehicleResponse response = new PagedVehicleResponse();
        response.setContent(list.stream().map(this::toSummaryResponse).toList());
        response.setPage(page != null ? page : 0);
        response.setSize(size != null ? size : 20);
        response.setTotalElements((long) list.size());
        response.setTotalPages(list.isEmpty() ? 0 : 1);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VehicleDetailedResponse> updateVehicle(UUID id, PatchVehicleRequest request) {
        log.info("Received request to update vehicle ID: {}", id);
        String make = request.getMake() != null ? request.getMake() : request.getBrand();
        Integer year = request.getManufactureYear() != null ? request.getManufactureYear() : request.getYear();
        Vehicle updated = updateVehicleUseCase.updateVehicle(id, make, request.getModel(), year);
        return ResponseEntity.ok(toDetailedResponse(updated));
    }

    @Override
    public ResponseEntity<Void> deleteVehicleById(UUID id) {
        log.info("Received request to delete vehicle ID: {}", id);
        deleteVehicleUseCase.deleteVehicleById(id);
        return ResponseEntity.noContent().build();
    }

    private VehicleDetailedResponse toDetailedResponse(Vehicle domain) {
        VehicleDetailedResponse response = new VehicleDetailedResponse();
        response.setId(domain.getId());
        response.setLicensePlate(domain.getLicensePlate());
        response.setMake(domain.getMake());
        response.setModel(domain.getModel());
        response.setManufactureYear(domain.getManufactureYear());
        if (domain.getCustomer() != null) {
            response.setCustomerDocument(domain.getCustomer().getDocument());
            VehicleCustomerItemResponse custItem = new VehicleCustomerItemResponse();
            custItem.setId(domain.getCustomer().getId());
            custItem.setName(domain.getCustomer().getName());
            custItem.setEmail(domain.getCustomer().getEmail());
            custItem.setDocument(domain.getCustomer().getDocument());
            response.setCustomer(custItem);
        }
        if (domain.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        if (domain.getUpdatedAt() != null) {
            response.setUpdatedAt(OffsetDateTime.ofInstant(domain.getUpdatedAt(), ZoneOffset.UTC));
        }
        return response;
    }

    private VehicleSummaryResponse toSummaryResponse(Vehicle domain) {
        VehicleSummaryResponse summary = new VehicleSummaryResponse();
        summary.setId(domain.getId());
        summary.setLicensePlate(domain.getLicensePlate());
        summary.setMake(domain.getMake());
        summary.setModel(domain.getModel());
        summary.setManufactureYear(domain.getManufactureYear());
        if (domain.getCustomer() != null) {
            summary.setCustomerDocument(domain.getCustomer().getDocument());
            VehicleCustomerItemResponse custItem = new VehicleCustomerItemResponse();
            custItem.setId(domain.getCustomer().getId());
            custItem.setName(domain.getCustomer().getName());
            custItem.setEmail(domain.getCustomer().getEmail());
            custItem.setDocument(domain.getCustomer().getDocument());
            summary.setCustomer(custItem);
        }
        if (domain.getCreatedAt() != null) {
            summary.setCreatedAt(OffsetDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC));
        }
        return summary;
    }
}
