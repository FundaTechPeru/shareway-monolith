package com.fundatechperu.shareway.payments.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Se generó la liquidación pendiente de un conductor por un viaje completado. */
public record DriverSettlementCreated(
    UUID settlementId,
    UUID tripId,
    UUID driverId,
    BigDecimal gross,
    BigDecimal commission,
    BigDecimal net,
    String currency,
    Instant occurredAt)
    implements DomainEvent {}
