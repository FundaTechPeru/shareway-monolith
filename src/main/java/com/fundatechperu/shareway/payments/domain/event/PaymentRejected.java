package com.fundatechperu.shareway.payments.domain.event;

import java.time.Instant;
import java.util.UUID;

/** La pasarela rechazó la autorización del pago de una reserva. */
public record PaymentRejected(UUID paymentId, UUID bookingId, UUID passengerId, Instant occurredAt)
    implements DomainEvent {}
