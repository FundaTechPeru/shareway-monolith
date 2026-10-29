package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverStatus;
import com.fundatechperu.shareway.drivermanagement.domain.model.EmergencyContact;
import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import java.util.UUID;

public record DriverProfileResponse(
    UUID userId,
    String fullName,
    String email,
    String licenseNumber,
    DriverStatus driverStatus,
    EmergencyContactResponse emergencyContact) {

  public record EmergencyContactResponse(String name, String phone, String relationship) {

    static EmergencyContactResponse from(EmergencyContact contact) {
      return new EmergencyContactResponse(
          contact.getName(), contact.getPhone(), contact.getRelationship());
    }
  }

  public static DriverProfileResponse from(User user) {
    return new DriverProfileResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getLicenseNumber(),
        user.getDriverStatus(),
        EmergencyContactResponse.from(user.getEmergencyContact()));
  }
}
