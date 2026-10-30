package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVehicleRepository extends JpaRepository<Vehicle, UUID> {

  Optional<Vehicle> findByIdAndDriverId(UUID id, UUID driverId);

  List<Vehicle> findAllByDriverIdOrderByCreatedAtAsc(UUID driverId);

  boolean existsByPlate(String plate);

  boolean existsByPlateAndIdNot(String plate, UUID id);
}
