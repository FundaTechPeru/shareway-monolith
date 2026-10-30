package com.fundatechperu.shareway.payments.interfaces.rest.dto;

import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.domain.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Vista de un pago con sus montos y marcas de tiempo. */
public record PaymentResponse(
    UUID id,
    UUID bookingId,
    UUID passengerId,
    UUID paymentMethodId,
    PaymentStatus status,
    BigDecimal authorizedAmount,
    BigDecimal capturedAmount,
    String currency,
    String providerRef,
    Instant authorizedAt,
    Instant capturedAt,
    Instant voidedAt) {

  public static PaymentResponse from(Payment payment) {
    return new PaymentResponse(
        payment.getId(),
        payment.getBookingId(),
        payment.getPassengerId(),
        payment.getPaymentMethodId(),
        payment.getStatus(),
        payment.getAuthorizedAmount().getAmount(),
        payment.getCapturedAmount() == null ? null : payment.getCapturedAmount().getAmount(),
        payment.getAuthorizedAmount().getCurrency(),
        payment.getProviderRef(),
        payment.getAuthorizedAt(),
        payment.getCapturedAt(),
        payment.getVoidedAt());
  }
}
