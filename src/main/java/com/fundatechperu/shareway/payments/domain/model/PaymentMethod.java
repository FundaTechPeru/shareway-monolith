package com.fundatechperu.shareway.payments.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Tarjeta tokenizada de un pasajero. Nunca almacena el número completo, solo el token, la marca y
 * los últimos 4 dígitos.
 */
@Entity
@Table(schema = "ps", name = "payment_methods")
public class PaymentMethod {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "passenger_id", nullable = false)
  private UUID passengerId;

  @Column(name = "card_token", nullable = false, length = 100)
  private String cardToken;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CardBrand brand;

  @Column(name = "last4", nullable = false, length = 4)
  private String last4;

  @Column(name = "exp_month", nullable = false)
  private int expMonth;

  @Column(name = "exp_year", nullable = false)
  private int expYear;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PaymentMethod() {}

  /** Registra una tarjeta nueva y activa; rechaza tarjetas vencidas respecto al mes actual. */
  public static PaymentMethod register(
      UUID passengerId,
      String cardToken,
      CardBrand brand,
      String last4,
      int expMonth,
      int expYear,
      YearMonth currentMonth) {
    if (YearMonth.of(expYear, expMonth).isBefore(currentMonth)) {
      throw new IllegalArgumentException("Card is expired");
    }
    PaymentMethod method = new PaymentMethod();
    method.passengerId = passengerId;
    method.cardToken = cardToken;
    method.brand = brand;
    method.last4 = last4;
    method.expMonth = expMonth;
    method.expYear = expYear;
    method.active = true;
    method.createdAt = Instant.now();
    return method;
  }

  /** Desactiva lógicamente la tarjeta sin borrarla. */
  public void deactivate() {
    this.active = false;
  }

  /** Indica si la tarjeta está activa y pertenece al pasajero indicado. */
  public boolean isUsableBy(UUID passengerId) {
    return active && this.passengerId.equals(passengerId);
  }

  public UUID getId() {
    return id;
  }

  public UUID getPassengerId() {
    return passengerId;
  }

  public String getCardToken() {
    return cardToken;
  }

  public CardBrand getBrand() {
    return brand;
  }

  public String getLast4() {
    return last4;
  }

  public int getExpMonth() {
    return expMonth;
  }

  public int getExpYear() {
    return expYear;
  }

  public boolean isActive() {
    return active;
  }
}
