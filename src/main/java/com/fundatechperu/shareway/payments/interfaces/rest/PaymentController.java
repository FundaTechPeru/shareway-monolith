package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.PaymentQueryService;
import com.fundatechperu.shareway.payments.interfaces.PaymentsExamples;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.PaymentResponse;
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
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta de los pagos del pasajero autenticado. */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Passenger payment history. Passengers only.")
public class PaymentController {

  private final PaymentQueryService queryService;

  public PaymentController(PaymentQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping("/me")
  @Operation(
      summary = "List the payments of the authenticated passenger",
      description =
          "Every payment of the passenger, newest first: AUTHORIZED when the seat was booked,"
              + " CAPTURED after the trip or a penalty, VOIDED after a free cancellation and"
              + " REJECTED when the card was declined.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Payments of the passenger",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class)),
                examples = @ExampleObject(value = PaymentsExamples.PAYMENT_LIST_RESPONSE))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing, invalid or expired token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.UNAUTHORIZED))),
    @ApiResponse(
        responseCode = "403",
        description = "The user is not a passenger",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN)))
  })
  public List<PaymentResponse> myPayments(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return queryService.listPassengerPayments(user.id()).stream()
        .map(PaymentResponse::from)
        .toList();
  }

  @GetMapping("/me/{id}")
  @Operation(
      summary = "Get one payment of the authenticated passenger",
      description = "A payment of another passenger is reported as not found.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Payment found",
        content =
            @Content(
                schema = @Schema(implementation = PaymentResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.PAYMENT_RESPONSE))),
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
        description = "The user is not a passenger",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.FORBIDDEN))),
    @ApiResponse(
        responseCode = "404",
        description = "Unknown payment, or it belongs to another passenger",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public PaymentResponse myPayment(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
      @Parameter(description = "Payment id", example = PaymentsExamples.PAYMENT_ID) @PathVariable
          UUID id) {
    return PaymentResponse.from(queryService.getPassengerPayment(user.id(), id));
  }
}
