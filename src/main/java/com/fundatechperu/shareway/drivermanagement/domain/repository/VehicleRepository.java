package com.fundatechperu.shareway.drivermanagement.domain.repository;

import com.fundatechperu.shareway.drivermanagement.domain.model.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository {

  Vehicle save(Vehicle vehicle);

  Optional<Vehicle> findByIdAndDriverId(UUID id, UUID driverId);

  List<Vehicle> findAllByDriverId(UUID driverId);

  boolean existsByPlate(String plate);

  boolean existsByPlateAndIdNot(String plate, UUID id);
}
