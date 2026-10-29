package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.AvailabilityService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.WeeklyAvailabilityRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.WeeklyAvailabilityResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers/me/availability")
@Tag(name = "Availability", description = "Weekly availability of the authenticated driver")
public class AvailabilityController {

  private final AvailabilityService availabilityService;

  public AvailabilityController(AvailabilityService availabilityService) {
    this.availabilityService = availabilityService;
  }

  @GetMapping
  @Operation(
      summary = "Get my weekly availability",
      description = "Drivers only. Slots ordered by day of week and start time.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Weekly availability of the driver",
        content =
            @Content(
                schema = @Schema(implementation = WeeklyAvailabilityResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.AVAILABILITY_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public WeeklyAvailabilityResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return WeeklyAvailabilityResponse.from(availabilityService.getWeeklySchedule(principal.id()));
  }

  @PutMapping
  @Operation(
      summary = "Replace my weekly availability",
      description =
          "Drivers only. Replaces the whole schedule; an empty list clears it. Times use HH:mm,"
              + " each slot needs startTime before endTime and slots on the same day must not"
              + " overlap (adjacent slots such as 07:00-09:00 and 09:00-11:00 are fine).",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "weekly",
                              value = DriverManagementExamples.AVAILABILITY_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Schedule replaced",
        content =
            @Content(
                schema = @Schema(implementation = WeeklyAvailabilityResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.AVAILABILITY_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Missing slots, invalid day or time, start not before end, overlapping slots",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.OVERLAPPING_SLOTS))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public WeeklyAvailabilityResponse replace(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody WeeklyAvailabilityRequest request) {
    return WeeklyAvailabilityResponse.from(
        availabilityService.replaceWeeklySchedule(principal.id(), request.toCommands()));
  }
}
