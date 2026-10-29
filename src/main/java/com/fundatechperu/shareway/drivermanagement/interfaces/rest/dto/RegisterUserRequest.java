package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.application.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank @Email @Size(max = 120) String email,
    @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$",
            message = "must contain at least one letter and one number")
        String password,
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank
        @Pattern(
            regexp = "^\\+?\\d{9,15}$",
            message = "must have 9 to 15 digits with an optional leading +")
        String phone) {

  public RegisterUserCommand toCommand() {
    return new RegisterUserCommand(email, password, fullName, phone);
  }
}
