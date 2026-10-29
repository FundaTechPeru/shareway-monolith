package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.domain.model.Vehicle;
import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
    UUID id,
    String plate,
    String brand,
    String model,
    int year,
    String color,
    int seats,
    Instant createdAt,
    Instant updatedAt) {

  public static VehicleResponse from(Vehicle vehicle) {
    return new VehicleResponse(
        vehicle.getId(),
        vehicle.getPlate(),
        vehicle.getBrand(),
        vehicle.getModel(),
        vehicle.getYear(),
        vehicle.getColor(),
        vehicle.getSeats(),
        vehicle.getCreatedAt(),
        vehicle.getUpdatedAt());
  }
}
