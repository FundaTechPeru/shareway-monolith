package com.fundatechperu.shareway.payments.domain.policy;

import com.fundatechperu.shareway.payments.domain.model.CancellationReason;
import com.fundatechperu.shareway.payments.domain.model.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Penaliza NO_SHOW y las cancelaciones del pasajero hechas con menos de la ventana configurada
 * antes de la salida, cobrando un porcentaje del monto autorizado. NO_DRIVER_AVAILABLE nunca se
 * penaliza.
 */
public class DefaultPenaltyPolicy implements PenaltyPolicy {

  private final BigDecimal rate;
  private final Duration window;

  public DefaultPenaltyPolicy(BigDecimal rate, long windowMinutes) {
    if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
      throw new IllegalArgumentException("Penalty rate must be between 0 and 1");
    }
    if (windowMinutes < 0) {
      throw new IllegalArgumentException("Penalty window must not be negative");
    }
    this.rate = rate;
    this.window = Duration.ofMinutes(windowMinutes);
  }

  @Override
  public Optional<Money> penaltyFor(
      CancellationReason reason,
      Instant cancelledAt,
      Instant scheduledDepartureAt,
      Money authorizedAmount) {
    boolean penalized =
        switch (reason) {
          case NO_SHOW -> true;
          case PASSENGER_CANCELLED ->
              Duration.between(cancelledAt, scheduledDepartureAt).compareTo(window) < 0;
          case NO_DRIVER_AVAILABLE -> false;
        };
    if (!penalized) {
      return Optional.empty();
    }
    Money penalty = authorizedAmount.multiply(rate);
    return penalty.isPositive() ? Optional.of(penalty) : Optional.empty();
  }
}
