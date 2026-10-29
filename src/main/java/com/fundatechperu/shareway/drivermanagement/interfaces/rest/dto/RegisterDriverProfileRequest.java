package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.application.RegisterDriverCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterDriverProfileRequest(
    @NotBlank
        @Pattern(
            regexp = "^[A-Z0-9]{8,12}$",
            message = "must be 8 to 12 uppercase letters or digits")
        String licenseNumber,
    @NotNull @Valid EmergencyContactRequest emergencyContact) {

  public RegisterDriverCommand toCommand() {
    return new RegisterDriverCommand(
        licenseNumber,
        emergencyContact.name(),
        emergencyContact.phone(),
        emergencyContact.relationship());
  }
}
