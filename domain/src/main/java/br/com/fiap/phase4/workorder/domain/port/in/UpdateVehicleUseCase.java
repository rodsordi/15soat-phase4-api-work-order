package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Vehicle;

import java.util.UUID;

public interface UpdateVehicleUseCase {
    Vehicle updateVehicle(UUID id, String make, String model, Integer manufactureYear);
}
