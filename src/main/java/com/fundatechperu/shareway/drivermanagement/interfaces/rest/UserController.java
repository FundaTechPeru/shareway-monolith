package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.UserProfileService;
import com.fundatechperu.shareway.drivermanagement.application.UserRegistrationService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.DriverProfileResponse;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.RegisterDriverProfileRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.UserResponse;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
  @Operation(
      summary = "Get my profile",
      description = "Returns the account of the user identified by the bearer token.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Profile of the authenticated user",
        content =
            @Content(
                schema = @Schema(implementation = UserResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.USER_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "404",
        description = "The user of the token no longer exists",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public UserResponse me(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return UserResponse.from(userProfileService.getProfile(principal.id()));
  }

  @PostMapping("/driver-profile")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Register as a driver",
      description =
          "Turns the passenger into a DRIVER with status PENDING_VERIFICATION. The license number"
              + " has 8 to 12 upper case letters or digits and must be unique. The new role"
              + " applies to the next request, without logging in again.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "driver",
                              value = DriverManagementExamples.REGISTER_DRIVER_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Driver profile created",
        content =
            @Content(
                schema = @Schema(implementation = DriverProfileResponse.class),
                examples =
                    @ExampleObject(value = DriverManagementExamples.DRIVER_PROFILE_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid license number or emergency contact",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "409",
        description = "Already a driver, or the license number is taken",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.CONFLICT)))
  })
  public DriverProfileResponse registerDriverProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody RegisterDriverProfileRequest request) {
    return DriverProfileResponse.from(
        userRegistrationService.registerDriver(principal.id(), request.toCommand()));
  }

  @GetMapping("/driver-profile")
  @Operation(
      summary = "Get my driver profile",
      description = "Returns the license, verification status and emergency contact.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Driver profile of the authenticated user",
        content =
            @Content(
                schema = @Schema(implementation = DriverProfileResponse.class),
                examples =
                    @ExampleObject(value = DriverManagementExamples.DRIVER_PROFILE_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "404",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public DriverProfileResponse driverProfile(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return DriverProfileResponse.from(userProfileService.getDriverProfile(principal.id()));
  }
}
