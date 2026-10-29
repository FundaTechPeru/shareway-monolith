package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.PaymentMethodService;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.PaymentMethodResponse;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.RegisterPaymentMethodRequest;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Payment Methods", description = "Passenger cards (tokenized)")
public class PaymentMethodController {

  private final PaymentMethodService paymentMethodService;

  public PaymentMethodController(PaymentMethodService paymentMethodService) {
    this.paymentMethodService = paymentMethodService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register a tokenized card for the authenticated passenger")
  public PaymentMethodResponse register(
      @AuthenticationPrincipal AuthenticatedUser user,
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
  @Operation(summary = "List the active cards of the authenticated passenger")
  public List<PaymentMethodResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
    return paymentMethodService.listActive(user.id()).stream()
        .map(PaymentMethodResponse::from)
        .toList();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Deactivate one of the authenticated passenger's cards")
  public void deactivate(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
    paymentMethodService.deactivate(user.id(), id);
  }
}
