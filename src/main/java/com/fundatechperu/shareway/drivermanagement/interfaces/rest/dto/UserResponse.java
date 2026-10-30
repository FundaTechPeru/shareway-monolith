package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverStatus;
import com.fundatechperu.shareway.drivermanagement.domain.model.Role;
import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    String phone,
    Role role,
    DriverStatus driverStatus,
    Instant createdAt) {

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getPhone(),
        user.getRole(),
        user.getDriverStatus(),
        user.getCreatedAt());
  }
}
