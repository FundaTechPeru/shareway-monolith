package com.fundatechperu.shareway.tripplanning.interfaces.rest.dto;

import com.fundatechperu.shareway.tripplanning.domain.model.DriverAvailability;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DriverAvailabilityResponse(
    UUID id,
    UUID driverId,
    String operationZone,
    LocalDate availableDate,
    LocalTime startTime,
    LocalTime endTime,
    int availableSeats) {

  public static DriverAvailabilityResponse from(DriverAvailability availability) {
    return new DriverAvailabilityResponse(
        availability.getId(),
        availability.getDriverId(),
        availability.getOperationZone(),
        availability.getAvailableDate(),
        availability.getStartTime(),
        availability.getEndTime(),
        availability.getAvailableSeats());
  }
}
