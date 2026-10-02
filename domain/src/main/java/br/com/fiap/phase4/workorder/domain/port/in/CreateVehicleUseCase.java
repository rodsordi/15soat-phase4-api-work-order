package br.com.fiap.phase4.workorder.domain.port.in;

import br.com.fiap.phase4.workorder.domain.model.Vehicle;

public interface CreateVehicleUseCase {
    Vehicle createVehicle(String customerDocument, String licensePlate, String make, String model, Integer manufactureYear);
}
