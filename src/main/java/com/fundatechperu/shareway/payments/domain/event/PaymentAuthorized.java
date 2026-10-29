package com.fundatechperu.shareway.payments.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** La pasarela aprobó la autorización del pago de una reserva. */
public record PaymentAuthorized(
    UUID paymentId,
    UUID bookingId,
    UUID passengerId,
    BigDecimal amount,
    String currency,
    Instant occurredAt)
    implements DomainEvent {}
