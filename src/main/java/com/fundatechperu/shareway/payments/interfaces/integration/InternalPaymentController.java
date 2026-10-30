package com.fundatechperu.shareway.payments.interfaces.integration;

import com.fundatechperu.shareway.payments.application.IdempotentResult;
import com.fundatechperu.shareway.payments.application.PaymentAuthorizationService;
import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.interfaces.PaymentsExamples;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.AuthorizePaymentRequest;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.PaymentResponse;
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

/** Endpoints internos de pagos invocados por otros microservicios con API key. */
@RestController
@RequestMapping("/api/v1/internal/payments")
@Tag(
    name = "Internal Payments",
    description =
        "Service-to-service payment operations. They take the X-Internal-Api-Key header instead"
            + " of a user token and the API gateway does not expose them.")
@SecurityRequirement(name = OpenApiConfig.INTERNAL_API_KEY_SCHEME)
public class InternalPaymentController {

  private final PaymentAuthorizationService authorizationService;

  public InternalPaymentController(PaymentAuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @PostMapping("/authorizations")
  @Operation(
      summary = "Authorize the payment of a booking (201 new, 200 idempotent replay)",
      description =
          "Holds the booking amount on one of the passenger's cards. Idempotent by bookingId: a"
              + " booking already AUTHORIZED or CAPTURED returns the same payment with 200 without"
              + " calling the gateway again, and a REJECTED one is retried on the same record. The"
              + " simulated gateway declines tok_declined and fails with tok_unavailable.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      examples =
                          @ExampleObject(
                              name = "authorization",
                              value = PaymentsExamples.AUTHORIZE_REQUEST))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Payment authorized",
        content =
            @Content(
                schema = @Schema(implementation = PaymentResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.PAYMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "200",
        description = "The booking was already authorized: same payment, gateway not called",
        content =
            @Content(
                schema = @Schema(implementation = PaymentResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.PAYMENT_RESPONSE))),
    @ApiResponse(
        responseCode = "400",
        description = "Missing ids, amount not positive or with more than 2 decimals, not PEN",
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
        responseCode = "402",
        description = "The gateway declined the card; the payment is stored as REJECTED",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.PAYMENT_DECLINED))),
    @ApiResponse(
        responseCode = "404",
        description = "The card does not exist, is inactive or belongs to another passenger",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = ErrorExamples.NOT_FOUND))),
    @ApiResponse(
        responseCode = "503",
        description = "The payment gateway is unavailable; nothing is authorized",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = PaymentsExamples.GATEWAY_UNAVAILABLE)))
  })
  public ResponseEntity<PaymentResponse> authorize(
      @Valid @RequestBody AuthorizePaymentRequest request) {
    IdempotentResult<Payment> result =
        authorizationService.authorize(
            request.bookingId(),
            request.passengerId(),
            request.paymentMethodId(),
            request.amount(),
            request.currency());
    return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
        .body(PaymentResponse.from(result.value()));
  }
}
