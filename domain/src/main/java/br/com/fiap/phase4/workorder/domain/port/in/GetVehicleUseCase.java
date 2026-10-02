package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Vehicle;

import java.util.UUID;

public interface GetVehicleUseCase {
    Vehicle getById(UUID id);
    Vehicle getByLicensePlate(String licensePlate);
}
