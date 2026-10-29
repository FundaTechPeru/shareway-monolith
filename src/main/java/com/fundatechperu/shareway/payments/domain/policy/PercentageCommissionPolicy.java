package com.fundatechperu.shareway.payments.domain.policy;

import com.fundatechperu.shareway.payments.domain.model.Money;
import java.math.BigDecimal;

/** Comisión como porcentaje fijo del bruto, redondeada a 2 decimales con HALF_UP. */
public class PercentageCommissionPolicy implements CommissionPolicy {

  private final BigDecimal rate;

  public PercentageCommissionPolicy(BigDecimal rate) {
    if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
      throw new IllegalArgumentException("Commission rate must be between 0 and 1");
    }
    this.rate = rate;
  }

  @Override
  public Money commissionFor(Money gross) {
    return gross.multiply(rate);
  }
}
