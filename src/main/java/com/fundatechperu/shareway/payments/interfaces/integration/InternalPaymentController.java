package com.fundatechperu.shareway.payments.interfaces.integration;

import com.fundatechperu.shareway.payments.application.IdempotentResult;
import com.fundatechperu.shareway.payments.application.PaymentAuthorizationService;
import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.AuthorizePaymentRequest;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.PaymentResponse;
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

/** Endpoints internos de pagos invocados por otros microservicios con API key. */
@RestController
@RequestMapping("/api/v1/internal/payments")
@Tag(name = "Internal Payments", description = "Service-to-service payment operations")
@SecurityRequirement(name = OpenApiConfig.INTERNAL_API_KEY_SCHEME)
public class InternalPaymentController {

  private final PaymentAuthorizationService authorizationService;

  public InternalPaymentController(PaymentAuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  @PostMapping("/authorizations")
  @Operation(summary = "Authorize the payment of a booking (201 new, 200 idempotent replay)")
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
