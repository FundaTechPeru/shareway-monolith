package com.fundatechperu.shareway.drivermanagement.domain.repository;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverAvailability;
import java.util.List;
import java.util.UUID;

public interface DriverAvailabilityRepository {

  List<DriverAvailability> findAllByDriverId(UUID driverId);

  void deleteAllByDriverId(UUID driverId);

  List<DriverAvailability> saveAll(List<DriverAvailability> slots);
}
