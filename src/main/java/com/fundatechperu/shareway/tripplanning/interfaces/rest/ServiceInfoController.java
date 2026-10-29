package com.fundatechperu.shareway.tripplanning.interfaces.rest;

import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Description of Trip Planning &amp; Matching, formerly served at {@code /} and {@code /api}. */
@RestController
@RequestMapping("/api/v1/info")
@Tag(name = "Service Info", description = "Trip Planning & Matching description")
public class ServiceInfoController {

  @GetMapping
  @Operation(
      summary = "Describe the Trip Planning & Matching endpoints",
      description =
          "Name, status and entry points of the context. The standalone service answered it at /"
              + " and /api; in the monolith it lives under the /api/v1 prefix.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Service description",
        content =
            @Content(
                examples = @ExampleObject(value = TripPlanningExamples.SERVICE_INFO_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED)))
  })
  public Map<String, Object> info() {
    return Map.of(
        "service", "ShareWay Trip Planning & Matching",
        "status", "UP",
        "description", "Trip requests, driver availability and matching proposals.",
        "health", "/actuator/health",
        "swagger", "/swagger-ui.html",
        "openapi", "/v3/api-docs/tripplanning",
        "endpoints",
            List.of(
                "GET /api/v1/trip-requests",
                "POST /api/v1/trip-requests",
                "POST /api/v1/driver-availabilities",
                "GET /api/v1/trip-requests/{tripRequestId}/match-proposals",
                "POST /api/v1/trip-requests/{tripRequestId}/match-proposals"));
  }
}
