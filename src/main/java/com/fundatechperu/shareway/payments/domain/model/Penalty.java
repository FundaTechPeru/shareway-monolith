package com.fundatechperu.shareway.payments.domain.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Penalidad cobrada al pasajero por cancelar tarde o no presentarse. */
@Entity
@Table(schema = "ps", name = "penalties")
public class Penalty {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;

  @Column(name = "booking_id", nullable = false, unique = true)
  private UUID bookingId;

  @Column(name = "passenger_id", nullable = false)
  private UUID passengerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CancellationReason reason;

  @Embedded
  @AttributeOverride(
      name = "amount",
      column = @Column(name = "amount", precision = 12, scale = 2, nullable = false))
  @AttributeOverride(
      name = "currency",
      column = @Column(name = "currency", length = 3, nullable = false))
  private Money amount;

  @Column(name = "applied_at", nullable = false)
  private Instant appliedAt;

  protected Penalty() {}

  /** Registra la penalidad cobrada sobre el pago de la reserva cancelada. */
  public static Penalty applyTo(
      Payment payment, CancellationReason reason, Money amount, Instant at) {
    Penalty penalty = new Penalty();
    penalty.paymentId = payment.getId();
    penalty.bookingId = payment.getBookingId();
    penalty.passengerId = payment.getPassengerId();
    penalty.reason = reason;
    penalty.amount = amount;
    penalty.appliedAt = at;
    return penalty;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPaymentId() {
    return paymentId;
  }

  public UUID getBookingId() {
    return bookingId;
  }

  public UUID getPassengerId() {
    return passengerId;
  }

  public CancellationReason getReason() {
    return reason;
  }

  public Money getAmount() {
    return amount;
  }

  public Instant getAppliedAt() {
    return appliedAt;
  }
}
