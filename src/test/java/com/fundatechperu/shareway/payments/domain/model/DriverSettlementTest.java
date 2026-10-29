package com.fundatechperu.shareway.payments.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.payments.domain.policy.PercentageCommissionPolicy;
import com.fundatechperu.shareway.shared.exception.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Pruebas unitarias del cálculo de comisión y neto de una liquidación. */
class DriverSettlementTest {

  private static final PercentageCommissionPolicy FIFTEEN_PERCENT =
      new PercentageCommissionPolicy(new BigDecimal("0.15"));

  @ParameterizedTest
  @CsvSource({
    "70.00, 10.50, 59.50",
    "33.33, 5.00, 28.33",
    "10.05, 1.51, 8.54",
    "0.03, 0.00, 0.03",
    "999.99, 150.00, 849.99"
  })
  void computesCommissionAndNetWithHalfUpRounding(String gross, String commission, String net) {
    DriverSettlement settlement = settlementFor(gross);

    assertThat(settlement.getCommission().getAmount()).isEqualTo(new BigDecimal(commission));
    assertThat(settlement.getNet().getAmount()).isEqualTo(new BigDecimal(net));
    assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
  }

  @Test
  void grossAlwaysEqualsCommissionPlusNet() {
    for (int cents = 1; cents <= 20_000; cents += 7) {
      DriverSettlement settlement = settlementFor(BigDecimal.valueOf(cents, 2).toPlainString());

      assertThat(settlement.getCommission().add(settlement.getNet()))
          .isEqualTo(settlement.getGross());
    }
  }

  @Test
  void marksPendingSettlementAsPaidOnlyOnce() {
    DriverSettlement settlement = settlementFor("50.00");

    settlement.markPaid(Instant.now());

    assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PAID);
    assertThat(settlement.getPaidAt()).isNotNull();
    assertThatThrownBy(() -> settlement.markPaid(Instant.now()))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void rejectsCommissionRateOutsideRange() {
    assertThatThrownBy(() -> new PercentageCommissionPolicy(new BigDecimal("1.5")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  private DriverSettlement settlementFor(String gross) {
    return DriverSettlement.create(
        new UUID(0, 1),
        new UUID(0, 2),
        Money.of(new BigDecimal(gross), "PEN"),
        Instant.now(),
        FIFTEEN_PERCENT);
  }
}
