package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverAvailability;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataDriverAvailabilityRepository
    extends JpaRepository<DriverAvailability, UUID> {

  List<DriverAvailability> findAllByDriverId(UUID driverId);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("delete from DriverAvailability a where a.driverId = :driverId")
  void deleteAllByDriverId(@Param("driverId") UUID driverId);
}
