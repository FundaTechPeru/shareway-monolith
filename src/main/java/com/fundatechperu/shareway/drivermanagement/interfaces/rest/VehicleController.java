package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.VehicleService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.VehicleRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.VehicleResponse;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
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
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/drivers/me/vehicles")
@Tag(name = "Vehicles", description = "Vehicles of the authenticated driver")
public class VehicleController {

  private final VehicleService vehicleService;

  public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Register a vehicle",
      description =
          "Drivers only; verification is not required. The plate has 6 letters or digits with an"
              + " optional hyphen and is stored as ABC-123, so abc123 and ABC-123 are the same"
              + " plate. The year goes from 2000 to next year and the seats from 1 to 8.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "vehicle",
                              value = DriverManagementExamples.VEHICLE_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Vehicle registered",
        content =
            @Content(
                schema = @Schema(implementation = VehicleResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.VEHICLE_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid plate, year, seats or missing fields",
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
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "409",
        description = "The plate is already registered",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.PLATE_TAKEN)))
  })
  public VehicleResponse register(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody VehicleRequest request) {
    return VehicleResponse.from(vehicleService.register(principal.id(), request.toCommand()));
  }

  @GetMapping
  @Operation(
      summary = "List my vehicles",
      description = "Drivers only. Vehicles of the driver, oldest first.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Vehicles of the driver",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = VehicleResponse.class)),
                examples = @ExampleObject(value = DriverManagementExamples.VEHICLE_LIST_RESPONSE))),
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
  public List<VehicleResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return vehicleService.listForDriver(principal.id()).stream()
        .map(VehicleResponse::from)
        .toList();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get one of my vehicles",
      description = "Drivers only. A vehicle of another driver is reported as not found.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Vehicle found",
        content =
            @Content(
                schema = @Schema(implementation = VehicleResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.VEHICLE_RESPONSE))),
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
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown vehicle, or it belongs to another driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public VehicleResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Parameter(description = "Vehicle id", example = DriverManagementExamples.VEHICLE_ID)
          @PathVariable
          UUID id) {
    return VehicleResponse.from(vehicleService.getForDriver(principal.id(), id));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update one of my vehicles",
      description =
          "Drivers only. Replaces every field of the vehicle; the new plate must not belong to"
              + " another vehicle.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "vehicle",
                              value = DriverManagementExamples.VEHICLE_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Vehicle updated",
        content =
            @Content(
                schema = @Schema(implementation = VehicleResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.VEHICLE_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid plate, year, seats or missing fields",
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
        responseCode = "403",
        description = "The user is not a driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown vehicle, or it belongs to another driver",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "409",
        description = "The new plate is already registered",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = DriverManagementExamples.PLATE_TAKEN)))
  })
  public VehicleResponse update(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Parameter(description = "Vehicle id", example = DriverManagementExamples.VEHICLE_ID)
          @PathVariable
          UUID id,
      @Valid @RequestBody VehicleRequest request) {
    return VehicleResponse.from(vehicleService.update(principal.id(), id, request.toCommand()));
  }
}
