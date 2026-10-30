package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverAvailability;
import com.fundatechperu.shareway.drivermanagement.domain.repository.DriverAvailabilityRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class DriverAvailabilityRepositoryAdapter implements DriverAvailabilityRepository {

  private static final Comparator<DriverAvailability> WEEKLY_ORDER =
      Comparator.comparing(DriverAvailability::getDayOfWeek)
          .thenComparing(DriverAvailability::getStartTime);

  private final SpringDataDriverAvailabilityRepository jpaRepository;

  public DriverAvailabilityRepositoryAdapter(SpringDataDriverAvailabilityRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public List<DriverAvailability> findAllByDriverId(UUID driverId) {
    return jpaRepository.findAllByDriverId(driverId).stream().sorted(WEEKLY_ORDER).toList();
  }

  @Override
  public void deleteAllByDriverId(UUID driverId) {
    jpaRepository.deleteAllByDriverId(driverId);
  }

  @Override
  public List<DriverAvailability> saveAll(List<DriverAvailability> slots) {
    return jpaRepository.saveAll(slots).stream().sorted(WEEKLY_ORDER).toList();
  }
}
