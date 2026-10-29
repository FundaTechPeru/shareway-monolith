package com.fundatechperu.shareway.drivermanagement.interfaces.rest;

import com.fundatechperu.shareway.drivermanagement.application.VehicleService;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.VehicleRequest;
import com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto.VehicleResponse;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
          "Drivers only; verification is not required. "
              + "Returns 409 if the plate is already registered.")
  public VehicleResponse register(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @Valid @RequestBody VehicleRequest request) {
    return VehicleResponse.from(vehicleService.register(principal.id(), request.toCommand()));
  }

  @GetMapping
  @Operation(summary = "List my vehicles")
  public List<VehicleResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal) {
    return vehicleService.listForDriver(principal.id()).stream()
        .map(VehicleResponse::from)
        .toList();
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get one of my vehicles",
      description = "Returns 404 if the vehicle is not mine.")
  public VehicleResponse get(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable UUID id) {
    return VehicleResponse.from(vehicleService.getForDriver(principal.id(), id));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update one of my vehicles",
      description = "Returns 404 if the vehicle is not mine.")
  public VehicleResponse update(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser principal,
      @PathVariable UUID id,
      @Valid @RequestBody VehicleRequest request) {
    return VehicleResponse.from(vehicleService.update(principal.id(), id, request.toCommand()));
  }
}
