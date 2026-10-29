package com.fundatechperu.shareway.payments.interfaces.rest.dto;

import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Vista de la liquidación de un viaje para el conductor o el administrador. */
public record SettlementResponse(
    UUID id,
    UUID tripId,
    UUID driverId,
    BigDecimal grossAmount,
    BigDecimal commissionAmount,
    BigDecimal netAmount,
    String currency,
    SettlementStatus status,
    Instant tripCompletedAt,
    Instant createdAt,
    Instant paidAt) {

  public static SettlementResponse from(DriverSettlement settlement) {
    return new SettlementResponse(
        settlement.getId(),
        settlement.getTripId(),
        settlement.getDriverId(),
        settlement.getGross().getAmount(),
        settlement.getCommission().getAmount(),
        settlement.getNet().getAmount(),
        settlement.getGross().getCurrency(),
        settlement.getStatus(),
        settlement.getTripCompletedAt(),
        settlement.getCreatedAt(),
        settlement.getPaidAt());
  }
}
