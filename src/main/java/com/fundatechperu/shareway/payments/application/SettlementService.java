package com.fundatechperu.shareway.payments.application;

import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.Money;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import com.fundatechperu.shareway.payments.domain.repository.DriverSettlementRepository;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta de ganancias del conductor y registro de pagos de liquidaciones por el administrador.
 */
@Service
public class SettlementService {

  private final DriverSettlementRepository settlements;

  public SettlementService(DriverSettlementRepository settlements) {
    this.settlements = settlements;
  }

  /** Totales de ganancias de un conductor, separando lo pendiente de lo ya pagado. */
  public record EarningsSummary(
      BigDecimal totalGross,
      BigDecimal totalCommission,
      BigDecimal totalNet,
      BigDecimal pendingNet,
      BigDecimal paidNet) {}

  @Transactional(readOnly = true)
  public List<DriverSettlement> listDriverSettlements(UUID driverId) {
    return settlements.findByDriverId(driverId);
  }

  /** Devuelve una liquidación del conductor; una ajena se trata como inexistente. */
  @Transactional(readOnly = true)
  public DriverSettlement getDriverSettlement(UUID driverId, UUID settlementId) {
    return settlements
        .findByIdAndDriverId(settlementId, driverId)
        .orElseThrow(() -> new ResourceNotFoundException("Settlement not found"));
  }

  @Transactional(readOnly = true)
  public EarningsSummary summarize(UUID driverId) {
    Money gross = Money.zero(Money.PEN);
    Money commission = Money.zero(Money.PEN);
    Money pending = Money.zero(Money.PEN);
    Money paid = Money.zero(Money.PEN);
    for (DriverSettlement settlement : settlements.findByDriverId(driverId)) {
      gross = gross.add(settlement.getGross());
      commission = commission.add(settlement.getCommission());
      if (settlement.getStatus() == SettlementStatus.PAID) {
        paid = paid.add(settlement.getNet());
      } else {
        pending = pending.add(settlement.getNet());
      }
    }
    return new EarningsSummary(
        gross.getAmount(),
        commission.getAmount(),
        pending.add(paid).getAmount(),
        pending.getAmount(),
        paid.getAmount());
  }

  @Transactional(readOnly = true)
  public List<DriverSettlement> listAll(Optional<SettlementStatus> status) {
    return settlements.findAll(status);
  }

  /**
   * Marca una liquidación PENDING como PAID.
   *
   * @throws com.fundatechperu.shareway.shared.exception.ConflictException si ya estaba pagada
   */
  @Transactional
  public DriverSettlement markPaid(UUID settlementId) {
    DriverSettlement settlement =
        settlements
            .findById(settlementId)
            .orElseThrow(() -> new ResourceNotFoundException("Settlement not found"));
    settlement.markPaid(Instant.now());
    return settlements.save(settlement);
  }
}
