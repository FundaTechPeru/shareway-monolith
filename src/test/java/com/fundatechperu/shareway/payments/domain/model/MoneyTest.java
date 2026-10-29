package com.fundatechperu.shareway.payments.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Pruebas unitarias del value object {@link Money}. */
class MoneyTest {

  @Test
  void normalizesToTwoDecimals() {
    assertThat(Money.of(new BigDecimal("10"), "PEN").getAmount())
        .isEqualTo(new BigDecimal("10.00"));
  }

  @Test
  void rejectsMoreThanTwoDecimals() {
    assertThatThrownBy(() -> Money.of(new BigDecimal("10.123"), "PEN"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsUnsupportedCurrency() {
    assertThatThrownBy(() -> Money.of(new BigDecimal("10.00"), "USD"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void multiplyRoundsHalfUp() {
    Money money = Money.of(new BigDecimal("10.05"), "PEN");

    assertThat(money.multiply(new BigDecimal("0.15")).getAmount())
        .isEqualTo(new BigDecimal("1.51"));
  }

  @Test
  void addsAndSubtracts() {
    Money a = Money.of(new BigDecimal("12.40"), "PEN");
    Money b = Money.of(new BigDecimal("2.15"), "PEN");

    assertThat(a.add(b)).isEqualTo(Money.of(new BigDecimal("14.55"), "PEN"));
    assertThat(a.subtract(b)).isEqualTo(Money.of(new BigDecimal("10.25"), "PEN"));
    assertThat(a.isGreaterThan(b)).isTrue();
  }
}
