package com.fundatechperu.shareway.tripplanning.interfaces.rest.dto;

import com.fundatechperu.shareway.tripplanning.application.TripRequestCommand;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateTripRequest(
    @NotNull UUID passengerId,
    @NotBlank String originLabel,
    @NotBlank String destinationLabel,
    @NotBlank String operationZone,
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal originLat,
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal originLng,
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal destinationLat,
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal destinationLng,
    @NotNull LocalDate travelDate,
    @NotNull LocalTime earliestDeparture,
    @NotNull LocalTime latestDeparture,
    @Min(1) int seatsRequested) {

  public TripRequestCommand toCommand() {
    return new TripRequestCommand(
        passengerId,
        originLabel,
        destinationLabel,
        operationZone,
        originLat,
        originLng,
        destinationLat,
        destinationLng,
        travelDate,
        earliestDeparture,
        latestDeparture,
        seatsRequested);
  }
}
