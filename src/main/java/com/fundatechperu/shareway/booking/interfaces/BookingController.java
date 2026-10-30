package com.fundatechperu.shareway.booking.interfaces;

import com.fundatechperu.shareway.booking.application.dto.request.CreateBookingRequest;
import com.fundatechperu.shareway.booking.application.dto.response.BookingResponse;
import com.fundatechperu.shareway.booking.application.dto.response.CancellationResponse;
import com.fundatechperu.shareway.booking.application.service.BookingService;
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
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Booking endpoints. The passenger is the authenticated user: the subject of the JWT validated by
 * the shared filter chain (the standalone service read an X-User-Id header instead).
 */
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Seat reservations: create, query and cancel bookings.")
public class BookingController {

  private static final String NO_SEATS_MESSAGE = "El viaje ya no tiene asientos disponibles.";

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @Operation(
      summary = "Create a booking (idempotent)",
      description =
          "Reserves one seat of the group with an optimistic lock and creates the booking in"
              + " status REQUESTED in a single local transaction, records the estimated fare,"
              + " starts the booking saga and publishes SeatHeld and"
              + " PaymentAuthorizationRequested. The passenger is the authenticated user;"
              + " repeating a request with the same Idempotency-Key returns the original booking"
              + " instead of creating a new one.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "booking",
                              value = BookingExamples.CREATE_BOOKING_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "202",
        description = "Booking accepted, or replayed via Idempotency-Key",
        content =
            @Content(
                schema = @Schema(implementation = BookingResponse.class),
                examples = @ExampleObject(value = BookingExamples.BOOKING_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "groupId or requestId is missing or is not a UUID",
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
        description =
            "Trip request not found for the passenger, group not found in seat inventories, or no"
                + " fare rule valid today",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = BookingExamples.GROUP_NOT_FOUND))),
    @ApiResponse(
        responseCode = "409",
        description = NO_SEATS_MESSAGE + " Also for a requestId that already has a booking.",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = BookingExamples.NO_SEATS)))
  })
  @PostMapping
  public ResponseEntity<BookingResponse> createBooking(
      @Valid @RequestBody CreateBookingRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser passenger,
      @Parameter(
              description =
                  "Replay key: the same key returns the original booking without creating another",
              example = "0a1b2c3d-booking-0001")
          @RequestHeader(value = "Idempotency-Key", required = false)
          String idempotencyKey) {
    BookingResponse response =
        bookingService.createBooking(request, passenger.id(), idempotencyKey);
    return ResponseEntity.accepted()
        .location(URI.create("/api/v1/bookings/" + response.bookingId()))
        .body(response);
  }

  @Operation(
      summary = "Get a booking by id",
      description = "Returns the booking with its status in uppercase (REQUESTED, GROUPED, ...).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Booking found",
        content =
            @Content(
                schema = @Schema(implementation = BookingResponse.class),
                examples = @ExampleObject(value = BookingExamples.BOOKING_RESPONSE))),
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
        description = "Unknown booking id",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = BookingExamples.BOOKING_NOT_FOUND)))
  })
  @GetMapping("/{bookingId}")
  public ResponseEntity<BookingResponse> findById(
      @Parameter(
              description = "Booking identifier",
              required = true,
              example = BookingExamples.BOOKING_ID)
          @PathVariable
          UUID bookingId) {
    return ResponseEntity.ok(bookingService.findById(bookingId));
  }

  @Operation(
      summary = "Cancel a booking",
      description =
          "Moves the booking to CANCELLED and releases its seat. Only bookings in REQUESTED,"
              + " GROUPED or CONFIRMED can be cancelled; cancelling twice answers 200 without"
              + " changes. A cancellation closer than 30 minutes to the departure also creates a"
              + " penalty of the 20 % of the estimated fare (minimum 1.00, reason"
              + " LATE_CANCELLATION), reported as penaltyAmount. Only the passenger of the"
              + " booking can cancel it; for anyone else it does not exist (404).")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Booking cancelled (idempotent)",
        content =
            @Content(
                schema = @Schema(implementation = CancellationResponse.class),
                examples = @ExampleObject(value = BookingExamples.CANCELLATION_RESPONSE))),
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
        description = "Unknown booking id, or the booking belongs to another passenger",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = BookingExamples.BOOKING_NOT_FOUND))),
    @ApiResponse(
        responseCode = "409",
        description =
            "The booking cannot be cancelled from its current status (boarded, in_progress or"
                + " completed)",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = BookingExamples.CANNOT_CANCEL)))
  })
  @PostMapping("/{bookingId}/cancel")
  public ResponseEntity<CancellationResponse> cancel(
      @Parameter(
              description = "Booking identifier",
              required = true,
              example = BookingExamples.BOOKING_ID)
          @PathVariable
          UUID bookingId,
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser passenger) {
    return ResponseEntity.ok(bookingService.cancel(bookingId, passenger.id()));
  }
}
