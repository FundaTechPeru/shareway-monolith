package com.fundatechperu.shareway.payments.interfaces.rest.dto;

import com.fundatechperu.shareway.payments.application.CancellationService.CancellationOutcome;
import com.fundatechperu.shareway.payments.domain.model.CancellationReason;
import com.fundatechperu.shareway.payments.domain.model.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

/** Resultado de procesar una cancelación: estado final del pago y penalidad cobrada, si hubo. */
public record CancellationResponse(
    UUID bookingId,
    UUID paymentId,
    PaymentStatus paymentStatus,
    CancellationReason reason,
    boolean penaltyApplied,
    UUID penaltyId,
    BigDecimal penaltyAmount,
    String currency) {

  public static CancellationResponse from(CancellationOutcome outcome) {
    var payment = outcome.payment();
    var penalty = outcome.penalty();
    return new CancellationResponse(
        payment.getBookingId(),
        payment.getId(),
        payment.getStatus(),
        payment.getCancellationReason(),
        penalty != null,
        penalty == null ? null : penalty.getId(),
        penalty == null ? BigDecimal.ZERO.setScale(2) : penalty.getAmount().getAmount(),
        payment.getAuthorizedAmount().getCurrency());
  }
}
