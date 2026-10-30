package com.fundatechperu.shareway.payments.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Se cobró el monto final de una reserva. */
public record PaymentCaptured(
    UUID paymentId, UUID bookingId, BigDecimal amount, String currency, Instant occurredAt)
    implements DomainEvent {}
