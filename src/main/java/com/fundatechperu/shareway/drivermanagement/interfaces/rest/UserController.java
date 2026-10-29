package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.UserProfileService;
import com.fundatechperu.shareway.drivermanagement.application.UserRegistrationService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.DriverProfileResponse;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.RegisterDriverProfileRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.UserResponse;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "Users", description = "Profile and driver profile of the authenticated user")
public class UserController {

  private final UserProfileService userProfileService;
  private final UserRegistrationService userRegistrationService;

  public UserController(
      UserProfileService userProfileService, UserRegistrationService userRegistrationService) {
    this.userProfileService = userProfileService;
    this.userRegistrationService = userRegistrationService;
  }

  @GetMapping
  @Operation(summary = "Get my profile")
  public UserResponse me(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return UserResponse.from(userProfileService.getProfile(principal.id()));
  }

  @PostMapping("/driver-profile")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Register as a driver",
      description =
          "Changes the role to DRIVER with status PENDING_VERIFICATION. "
              + "Returns 409 if already a driver or the license number is taken.")
  public DriverProfileResponse registerDriverProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody RegisterDriverProfileRequest request) {
    return DriverProfileResponse.from(
        userRegistrationService.registerDriver(principal.id(), request.toCommand()));
  }

  @GetMapping("/driver-profile")
  @Operation(
      summary = "Get my driver profile",
      description = "Returns 404 if the user is not a driver.")
  public DriverProfileResponse driverProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return DriverProfileResponse.from(userProfileService.getDriverProfile(principal.id()));
  }
}
