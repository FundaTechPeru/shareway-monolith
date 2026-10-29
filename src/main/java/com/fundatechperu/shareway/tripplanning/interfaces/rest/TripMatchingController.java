package com.fundatechperu.shareway.tripplanning.interfaces.rest;

import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import com.fundatechperu.shareway.tripplanning.application.TripMatchingService;
import com.fundatechperu.shareway.tripplanning.interfaces.rest.dto.CreateDriverAvailabilityRequest;
import com.fundatechperu.shareway.tripplanning.interfaces.rest.dto.CreateTripRequest;
import com.fundatechperu.shareway.tripplanning.interfaces.rest.dto.DriverAvailabilityResponse;
import com.fundatechperu.shareway.tripplanning.interfaces.rest.dto.MatchProposalResponse;
import com.fundatechperu.shareway.tripplanning.interfaces.rest.dto.TripRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(
    name = "Trip Planning & Matching",
    description = "Trip requests, driver availability and match proposals")
public class TripMatchingController {

  private final TripMatchingService tripMatchingService;

  public TripMatchingController(TripMatchingService tripMatchingService) {
    this.tripMatchingService = tripMatchingService;
  }

  @PostMapping("/trip-requests")
  @Operation(
      summary = "Create trip request",
      description =
          "Registers a passenger trip request (origin, destination, operation zone, date,"
              + " departure window and seats) to search compatible driver availability. It starts"
              + " in PENDING_MATCH.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "tripRequest",
                              value = TripPlanningExamples.CREATE_TRIP_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Trip request registered; Location points to it",
        content =
            @Content(
                schema = @Schema(implementation = TripRequestResponse.class),
                examples = @ExampleObject(value = TripPlanningExamples.TRIP_REQUEST_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Missing fields, coordinates out of range or fewer than one seat",
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
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED)))
  })
  public ResponseEntity<TripRequestResponse> createTripRequest(
      @Valid @RequestBody CreateTripRequest request) {
    TripRequestResponse response =
        TripRequestResponse.from(tripMatchingService.createTripRequest(request.toCommand()));
    return ResponseEntity.created(URI.create("/api/v1/trip-requests/" + response.id()))
        .body(response);
  }

  @GetMapping("/trip-requests")
  @Operation(
      summary = "List trip requests",
      description = "Returns the trip requests registered in the service, newest first.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Trip requests",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = TripRequestResponse.class)),
                examples =
                    @ExampleObject(value = TripPlanningExamples.TRIP_REQUEST_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED)))
  })
  public ResponseEntity<List<TripRequestResponse>> listTripRequests() {
    List<TripRequestResponse> responses =
        tripMatchingService.listTripRequests().stream().map(TripRequestResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  @PostMapping("/driver-availabilities")
  @Operation(
      summary = "Register driver availability",
      description =
          "Registers the operating zone, date, time window and seats available for a driver. This"
              + " dated availability feeds the matching; the weekly schedule of a driver lives in"
              + " Driver Management (/api/v1/drivers/me/availability).",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "availability",
                              value = TripPlanningExamples.CREATE_AVAILABILITY_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Availability registered; Location points to it",
        content =
            @Content(
                schema = @Schema(implementation = DriverAvailabilityResponse.class),
                examples = @ExampleObject(value = TripPlanningExamples.AVAILABILITY_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Missing fields or fewer than one seat",
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
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED)))
  })
  public ResponseEntity<DriverAvailabilityResponse> createDriverAvailability(
      @Valid @RequestBody CreateDriverAvailabilityRequest request) {
    DriverAvailabilityResponse response =
        DriverAvailabilityResponse.from(
            tripMatchingService.createDriverAvailability(request.toCommand()));
    return ResponseEntity.created(URI.create("/api/v1/driver-availabilities/" + response.id()))
        .body(response);
  }

  @PostMapping("/trip-requests/{tripRequestId}/match-proposals")
  @Operation(
      summary = "Generate match proposals",
      description =
          "Proposes up to five drivers whose availability is in the same zone (case insensitive)"
              + " and date, overlaps the departure window and has enough seats, fewest spare seats"
              + " first. Fare = 4.50 + 3.20 per requested seat; duration = departure window in"
              + " minutes, at least 15. Idempotent: once proposals exist they are returned again.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Proposals of the trip request (empty when no driver is compatible)",
        content =
            @Content(
                array =
                    @ArraySchema(schema = @Schema(implementation = MatchProposalResponse.class)),
                examples = @ExampleObject(value = TripPlanningExamples.PROPOSAL_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a UUID",
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
        responseCode = "404",
        description = "Unknown trip request",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = TripPlanningExamples.TRIP_REQUEST_NOT_FOUND)))
  })
  public ResponseEntity<List<MatchProposalResponse>> generateMatchProposals(
      @Parameter(description = "Trip request id", example = TripPlanningExamples.TRIP_REQUEST_ID)
          @PathVariable
          UUID tripRequestId) {
    List<MatchProposalResponse> responses =
        tripMatchingService.generateMatchProposals(tripRequestId).stream()
            .map(MatchProposalResponse::from)
            .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/trip-requests/{tripRequestId}/match-proposals")
  @Operation(
      summary = "List match proposals",
      description =
          "Returns the proposals already generated for a trip request, one per driver; an"
              + " unknown or not yet matched trip request gives an empty list.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Proposals of the trip request",
        content =
            @Content(
                array =
                    @ArraySchema(schema = @Schema(implementation = MatchProposalResponse.class)),
                examples = @ExampleObject(value = TripPlanningExamples.PROPOSAL_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "The id is not a UUID",
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
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED)))
  })
  public ResponseEntity<List<MatchProposalResponse>> listMatchProposals(
      @Parameter(description = "Trip request id", example = TripPlanningExamples.TRIP_REQUEST_ID)
          @PathVariable
          UUID tripRequestId) {
    List<MatchProposalResponse> responses =
        tripMatchingService.listMatchProposals(tripRequestId).stream()
            .map(MatchProposalResponse::from)
            .toList();
    return ResponseEntity.ok(responses);
  }
}
