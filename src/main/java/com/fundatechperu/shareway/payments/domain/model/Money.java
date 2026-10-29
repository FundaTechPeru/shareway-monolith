package com.fundatechperu.shareway.payments.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object monetario: {@link BigDecimal} con 2 decimales, redondeo HALF_UP y moneda única PEN.
 */
@Embeddable
public class Money {

  public static final String PEN = "PEN";
  private static final int SCALE = 2;

  @Column(precision = 12, scale = SCALE)
  private BigDecimal amount;

  @Column(length = 3)
  private String currency;

  protected Money() {}

  private Money(BigDecimal amount, String currency) {
    this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
    this.currency = currency;
  }

  /**
   * Crea un monto validando que la moneda sea soportada y que no tenga más de 2 decimales.
   *
   * @throws IllegalArgumentException si la moneda no es PEN o el monto tiene más de 2 decimales
   */
  public static Money of(BigDecimal amount, String currency) {
    Objects.requireNonNull(amount, "amount");
    if (!PEN.equals(currency)) {
      throw new IllegalArgumentException("Unsupported currency: " + currency);
    }
    if (amount.stripTrailingZeros().scale() > SCALE) {
      throw new IllegalArgumentException("Amount must have at most 2 decimals");
    }
    return new Money(amount, currency);
  }

  public static Money zero(String currency) {
    return of(BigDecimal.ZERO, currency);
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  /** Multiplica por una tasa y redondea el resultado a 2 decimales con HALF_UP. */
  public Money multiply(BigDecimal rate) {
    return new Money(amount.multiply(rate), currency);
  }

  public boolean isGreaterThan(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  private void requireSameCurrency(Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + currency + " vs " + other.currency);
    }
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Money other
        && amount.compareTo(other.amount) == 0
        && currency.equals(other.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount.stripTrailingZeros(), currency);
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency;
  }
}
