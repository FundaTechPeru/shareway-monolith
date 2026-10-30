package com.fundatechperu.shareway.tripplanning.interfaces.rest.dto;

import com.fundatechperu.shareway.tripplanning.application.DriverAvailabilityCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateDriverAvailabilityRequest(
    @NotNull UUID driverId,
    @NotBlank String operationZone,
    @NotNull LocalDate availableDate,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @Min(1) int availableSeats) {

  public DriverAvailabilityCommand toCommand() {
    return new DriverAvailabilityCommand(
        driverId, operationZone, availableDate, startTime, endTime, availableSeats);
  }
}
