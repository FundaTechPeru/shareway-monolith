package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.AvailabilityService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.WeeklyAvailabilityRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.WeeklyAvailabilityResponse;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
  @Operation(summary = "Get my weekly availability")
  public WeeklyAvailabilityResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return WeeklyAvailabilityResponse.from(availabilityService.getWeeklySchedule(principal.id()));
  }

  @PutMapping
  @Operation(
      summary = "Replace my weekly availability",
      description =
          "Replaces the whole schedule. Each slot needs startTime before endTime and slots on "
              + "the same day must not overlap.")
  public WeeklyAvailabilityResponse replace(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody WeeklyAvailabilityRequest request) {
    return WeeklyAvailabilityResponse.from(
        availabilityService.replaceWeeklySchedule(principal.id(), request.toCommands()));
  }
}
