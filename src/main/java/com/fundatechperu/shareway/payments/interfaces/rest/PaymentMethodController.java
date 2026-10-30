package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.PaymentMethodService;
import com.fundatechperu.shareway.payments.interfaces.PaymentsExamples;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.PaymentMethodResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.RegisterPaymentMethodRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints del pasajero para gestionar sus tarjetas. */
@RestController
@RequestMapping("/api/v1/payment-methods")
@Tag(name = "Payment Methods", description = "Passenger cards (tokenized). Passengers only.")
public class PaymentMethodController {

  private final PaymentMethodService paymentMethodService;

  public PaymentMethodController(PaymentMethodService paymentMethodService) {
    this.paymentMethodService = paymentMethodService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Register a tokenized card for the authenticated passenger",
      description =
          "Stores only the card token, brand and last 4 digits, never the full number. The card"
              + " must not be expired and a passenger cannot register the same token twice. The"
              + " token is not returned by any endpoint.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "visa",
                              value = PaymentsExamples.REGISTER_CARD_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Card registered and active",
        content =
            @Content(
                schema = @Schema(implementation = PaymentMethodResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.CARD_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid brand, last 4 digits or expiry, or the card is expired",
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
        responseCode = "409",
        description = "The passenger already has this card token",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.CARD_TAKEN)))
  })
  public PaymentMethodResponse register(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
      @Valid @RequestBody RegisterPaymentMethodRequest request) {
    return PaymentMethodResponse.from(
        paymentMethodService.register(
            user.id(),
            request.cardToken(),
            request.brand(),
            request.last4(),
            request.expMonth(),
            request.expYear()));
  }

  @GetMapping
  @Operation(
      summary = "List the active cards of the authenticated passenger",
      description = "Deactivated cards are not listed. Oldest card first.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Active cards of the passenger",
        content =
            @Content(
                array =
                    @ArraySchema(schema = @Schema(implementation = PaymentMethodResponse.class)),
                examples = @ExampleObject(value = PaymentsExamples.CARD_LIST_RESPONSE))),
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
  public List<PaymentMethodResponse> list(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
    return paymentMethodService.listActive(user.id()).stream()
        .map(PaymentMethodResponse::from)
        .toList();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Deactivate one of the authenticated passenger's cards",
      description =
          "Logical delete: the card stays stored for the payment history but can no longer be"
              + " used. A card of another passenger is reported as not found.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Card deactivated"),
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
        description = "Unknown or already inactive card, or it belongs to another passenger",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND)))
  })
  public void deactivate(
      @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
      @Parameter(description = "Card id", example = PaymentsExamples.PAYMENT_METHOD_ID)
          @PathVariable
          UUID id) {
    paymentMethodService.deactivate(user.id(), id);
  }
}
