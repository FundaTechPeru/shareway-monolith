package com.fundatechperu.shareway.payments.interfaces.rest.dto;

import com.fundatechperu.shareway.payments.application.SettlementService.EarningsSummary;
import java.math.BigDecimal;

/** Resumen de ganancias del conductor autenticado. */
public record EarningsSummaryResponse(
    BigDecimal totalGross,
    BigDecimal totalCommission,
    BigDecimal totalNet,
    BigDecimal pendingNet,
    BigDecimal paidNet) {

  public static EarningsSummaryResponse from(EarningsSummary summary) {
    return new EarningsSummaryResponse(
        summary.totalGross(),
        summary.totalCommission(),
        summary.totalNet(),
        summary.pendingNet(),
        summary.paidNet());
  }
}
