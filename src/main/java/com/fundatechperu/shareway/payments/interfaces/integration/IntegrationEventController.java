package com.fundatechperu.shareway.payments.interfaces.integration;

import com.fundatechperu.shareway.payments.application.CancellationService;
import com.fundatechperu.shareway.payments.application.IdempotentResult;
import com.fundatechperu.shareway.payments.application.PaymentCaptureService;
import com.fundatechperu.shareway.payments.application.PaymentCaptureService.TripCharge;
import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.interfaces.PaymentsExamples;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.BookingCancelledRequest;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.CancellationResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.SettlementResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.TripCompletedRequest;
import com.fundatechperu.shareway.shared.openapi.ErrorExamples;
import com.fundatechperu.shareway.shared.openapi.OpenApiConfig;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(
    name = "Integration Events",
    description =
        "Domain events received from other services (Trip Execution and, once extracted,"
            + " Booking & Pricing). They take the X-Internal-Api-Key header instead of a user"
            + " token and the API gateway does not expose them.")
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
  @Operation(
      summary = "TripCompleted: capture the trip bookings and create the driver settlement",
      description =
          "In one transaction captures the final amount of every booking (never above what was"
              + " authorized) and creates the PENDING settlement: gross = sum captured,"
              + " commission = gross x COMMISSION_RATE rounded HALF_UP, net = gross - commission."
              + " Idempotent by tripId: a repeated event returns the same settlement with 200.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "tripCompleted",
                              value = PaymentsExamples.TRIP_COMPLETED_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Payments captured and settlement created",
        content =
            @Content(
                schema = @Schema(implementation = SettlementResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.SETTLEMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "200",
        description = "The trip was already settled: same settlement, nothing captured again",
        content =
            @Content(
                schema = @Schema(implementation = SettlementResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.SETTLEMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Missing ids or completion time, no bookings, final amount not positive",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing or wrong X-Internal-Api-Key",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.INVALID_API_KEY))),
    @ApiResponse(
        responseCode = "409",
        description =
            "A booking has no AUTHORIZED payment or its final amount exceeds the authorized one;"
                + " nothing changes",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.CAPTURE_CONFLICT)))
  })
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
      summary = "BookingCancelled: charge the cancellation penalty or void the authorization",
      description =
          "NO_SHOW and a PASSENGER_CANCELLED less than PENALTY_WINDOW_MINUTES (30) before the"
              + " departure charge PENALTY_RATE (50 %) of the authorized amount; any other"
              + " cancellation, and NO_DRIVER_AVAILABLE always, voids the authorization."
              + " Idempotent by bookingId.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "lateCancellation",
                              value = PaymentsExamples.BOOKING_CANCELLED_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Penalty charged or authorization voided",
        content =
            @Content(
                schema = @Schema(implementation = CancellationResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.CANCELLATION_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Missing booking id or times, or unknown reason",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.BAD_REQUEST))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing or wrong X-Internal-Api-Key",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.INVALID_API_KEY))),
    @ApiResponse(
        responseCode = "404",
        description = "The booking has no payment",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "409",
        description = "The payment is not AUTHORIZED (for example, already captured)",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.PAYMENT_NOT_AUTHORIZED)))
  })
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
