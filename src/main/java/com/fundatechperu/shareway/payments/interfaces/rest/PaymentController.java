package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.application.PaymentQueryService;
import com.fundatechperu.shareway.payments.interfaces.rest.dto.PaymentResponse;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Payments", description = "Passenger payment history")
public class PaymentController {

  private final PaymentQueryService queryService;

  public PaymentController(PaymentQueryService queryService) {
    this.queryService = queryService;
  }

  @GetMapping("/me")
  @Operation(summary = "List the payments of the authenticated passenger")
  public List<PaymentResponse> myPayments(@AuthenticationPrincipal AuthenticatedUser user) {
    return queryService.listPassengerPayments(user.id()).stream()
        .map(PaymentResponse::from)
        .toList();
  }

  @GetMapping("/me/{id}")
  @Operation(summary = "Get one payment of the authenticated passenger")
  public PaymentResponse myPayment(
      @AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID id) {
    return PaymentResponse.from(queryService.getPassengerPayment(user.id(), id));
  }
}
