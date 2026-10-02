package br.com.fiap.phase4.workorder.adapter.in.web;

import br.com.fiap.phase4.workorder.adapter.in.web.api.VehicleApi;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.CreateVehicleRequest;
import br.com.fiap.phase4.workorder.adapter.in.web.dto.VehicleResponse;
import br.com.fiap.phase4.workorder.domain.model.Vehicle;
import br.com.fiap.phase4.workorder.domain.port.in.CreateVehicleUseCase;
import br.com.fiap.phase4.workorder.domain.port.in.GetVehicleUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VehicleController implements VehicleApi {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;

    @Override
    public ResponseEntity<VehicleResponse> createVehicle(CreateVehicleRequest request) {
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
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(vehicle));
    }

    @Override
    public ResponseEntity<VehicleResponse> getVehicleByLicensePlate(String licensePlate) {
        log.info("Received request to get vehicle by license plate: {}", licensePlate);
        Vehicle vehicle = getVehicleUseCase.getByLicensePlate(licensePlate);
        return ResponseEntity.ok(toResponse(vehicle));
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        if (vehicle.getCustomer() != null) {
            response.setCustomerDocument(vehicle.getCustomer().getDocument());
        }
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setMake(vehicle.getMake());
        response.setModel(vehicle.getModel());
        response.setManufactureYear(vehicle.getManufactureYear());
        if (vehicle.getCreatedAt() != null) {
            response.setCreatedAt(OffsetDateTime.ofInstant(vehicle.getCreatedAt(), ZoneOffset.UTC));
        }
        return response;
    }
}
