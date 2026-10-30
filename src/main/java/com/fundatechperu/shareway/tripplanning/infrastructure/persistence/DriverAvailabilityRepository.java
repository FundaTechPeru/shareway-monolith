package com.fundatechperu.shareway.tripplanning.infrastructure.persistence;

import com.fundatechperu.shareway.tripplanning.domain.model.DriverAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DriverAvailabilityRepository extends JpaRepository<DriverAvailability, UUID> {

  @Query(
      """
      select availability
      from TripPlanningDriverAvailability availability
      where lower(availability.operationZone) = lower(:operationZone)
        and availability.availableDate = :travelDate
        and availability.startTime <= :latestDeparture
        and availability.endTime >= :earliestDeparture
        and availability.availableSeats >= :seatsRequested
      order by availability.availableSeats asc, availability.startTime asc
      """)
  List<DriverAvailability> findCandidates(
      @Param("operationZone") String operationZone,
      @Param("travelDate") LocalDate travelDate,
      @Param("earliestDeparture") LocalTime earliestDeparture,
      @Param("latestDeparture") LocalTime latestDeparture,
      @Param("seatsRequested") int seatsRequested);
}
