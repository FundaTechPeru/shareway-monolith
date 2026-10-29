package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.application.VehicleCommand;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.validation.ManufactureYear;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
    @NotBlank
        @Pattern(
            regexp = "^[A-Za-z0-9]{3}-?[A-Za-z0-9]{3}$",
            message = "must have 6 letters or digits with an optional hyphen")
        String plate,
    @NotBlank @Size(max = 50) String brand,
    @NotBlank @Size(max = 50) String model,
    @NotNull @ManufactureYear Integer year,
    @NotBlank @Size(max = 30) String color,
    @NotNull @Min(1) @Max(8) Integer seats) {

  public VehicleCommand toCommand() {
    return new VehicleCommand(plate, brand, model, year, color, seats);
  }
}
