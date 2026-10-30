package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.Vehicle;
import com.fundatechperu.shareway.drivermanagement.domain.repository.VehicleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleRepositoryAdapter implements VehicleRepository {

  private final SpringDataVehicleRepository jpaRepository;

  public VehicleRepositoryAdapter(SpringDataVehicleRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Vehicle save(Vehicle vehicle) {
    return jpaRepository.save(vehicle);
  }

  @Override
  public Optional<Vehicle> findByIdAndDriverId(UUID id, UUID driverId) {
    return jpaRepository.findByIdAndDriverId(id, driverId);
  }

  @Override
  public List<Vehicle> findAllByDriverId(UUID driverId) {
    return jpaRepository.findAllByDriverIdOrderByCreatedAtAsc(driverId);
  }

  @Override
  public boolean existsByPlate(String plate) {
    return jpaRepository.existsByPlate(plate);
  }

  @Override
  public boolean existsByPlateAndIdNot(String plate, UUID id) {
    return jpaRepository.existsByPlateAndIdNot(plate, id);
  }
}
