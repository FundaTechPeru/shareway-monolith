package com.fundatechperu.shareway.payments.domain.event;

import com.fundatechperu.shareway.payments.domain.model.CancellationReason;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Se cobró una penalidad por cancelación tardía o no presentación. */
public record PenaltyApplied(
    UUID penaltyId,
    UUID paymentId,
    UUID bookingId,
    UUID passengerId,
    BigDecimal amount,
    String currency,
    CancellationReason reason,
    Instant occurredAt)
    implements DomainEvent {}
