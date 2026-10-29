package com.fundatechperu.shareway.payments.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fundatechperu.shareway.payments.domain.model.CancellationReason;
import com.fundatechperu.shareway.payments.domain.model.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias de la política de penalidad dentro y fuera de la ventana de cancelación. */
class DefaultPenaltyPolicyTest {

  private static final Instant DEPARTURE = Instant.parse("2026-10-10T08:00:00Z");
  private static final Money AUTHORIZED = Money.of(new BigDecimal("25.25"), "PEN");

  private final DefaultPenaltyPolicy policy = new DefaultPenaltyPolicy(new BigDecimal("0.50"), 30);

  @Test
  void passengerCancellationInsideWindowIsPenalized() {
    Optional<Money> penalty =
        evaluate(CancellationReason.PASSENGER_CANCELLED, Duration.ofMinutes(29));

    assertThat(penalty).contains(Money.of(new BigDecimal("12.63"), "PEN"));
  }

  @Test
  void passengerCancellationAfterDepartureIsPenalized() {
    assertThat(evaluate(CancellationReason.PASSENGER_CANCELLED, Duration.ofMinutes(-5)))
        .isPresent();
  }

  @Test
  void passengerCancellationExactlyAtWindowLimitIsFree() {
    assertThat(evaluate(CancellationReason.PASSENGER_CANCELLED, Duration.ofMinutes(30))).isEmpty();
  }

  @Test
  void passengerCancellationOutsideWindowIsFree() {
    assertThat(evaluate(CancellationReason.PASSENGER_CANCELLED, Duration.ofHours(3))).isEmpty();
  }

  @Test
  void noShowIsAlwaysPenalized() {
    assertThat(evaluate(CancellationReason.NO_SHOW, Duration.ofHours(3))).isPresent();
  }

  @Test
  void noDriverAvailableIsNeverPenalized() {
    assertThat(evaluate(CancellationReason.NO_DRIVER_AVAILABLE, Duration.ofMinutes(1))).isEmpty();
  }

  private Optional<Money> evaluate(CancellationReason reason, Duration beforeDeparture) {
    return policy.penaltyFor(reason, DEPARTURE.minus(beforeDeparture), DEPARTURE, AUTHORIZED);
  }
}
