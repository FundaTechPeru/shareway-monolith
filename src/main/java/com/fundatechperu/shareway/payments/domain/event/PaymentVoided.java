package com.fundatechperu.shareway.payments.domain.event;

import com.fundatechperu.shareway.payments.domain.model.CancellationReason;
import java.time.Instant;
import java.util.UUID;

/** Se anuló la autorización de una reserva cancelada sin penalidad. */
public record PaymentVoided(
    UUID paymentId, UUID bookingId, CancellationReason reason, Instant occurredAt)
    implements DomainEvent {}
