package com.fundatechperu.shareway.payments.interfaces.integration;

import com.fundatechperu.shareway.payments.application.CancellationService;
import com.fundatechperu.shareway.payments.application.IdempotentResult;
import com.fundatechperu.shareway.payments.application.PaymentCaptureService;
import com.fundatechperu.shareway.payments.application.PaymentCaptureService.TripCharge;
import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.BookingCancelledRequest;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.CancellationResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.SettlementResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.TripCompletedRequest;
import com.fundatechperu.shareway.shared.openapi.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Recepción de los Domain Events de otros servicios mientras no exista un broker de mensajes. */
@RestController
@RequestMapping("/api/v1/internal/events")
@Tag(name = "Integration Events", description = "Domain events received from other services")
@SecurityRequirement(name = OpenApiConfig.INTERNAL_API_KEY_SCHEME)
public class IntegrationEventController {

  private final PaymentCaptureService captureService;
  private final CancellationService cancellationService;

  public IntegrationEventController(
      PaymentCaptureService captureService, CancellationService cancellationService) {
    this.captureService = captureService;
    this.cancellationService = cancellationService;
  }

  @PostMapping("/trip-completed")
  @Operation(summary = "TripCompleted: capture the trip bookings and create the driver settlement")
  public ResponseEntity<SettlementResponse> tripCompleted(
      @Valid @RequestBody TripCompletedRequest request) {
    IdempotentResult<DriverSettlement> result =
        captureService.captureTrip(
            request.tripId(),
            request.driverId(),
            request.completedAt(),
            request.bookings().stream()
                .map(charge -> new TripCharge(charge.bookingId(), charge.finalAmount()))
                .toList());
    return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(SettlementResponse.from(result.value()));
  }

  @PostMapping("/booking-cancelled")
  @Operation(
      summary = "BookingCancelled: charge the cancellation penalty or void the authorization")
  public CancellationResponse bookingCancelled(
      @Valid @RequestBody BookingCancelledRequest request) {
    return CancellationResponse.from(
        cancellationService.cancel(
            request.bookingId(),
            request.reason(),
            request.cancelledAt(),
            request.scheduledDepartureAt()));
  }
}
