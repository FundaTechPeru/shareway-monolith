package com.fundatechperu.shareway.payments.domain.model;

import com.fundatechperu.shareway.shared.exception.ConflictException;
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

/**
 * Agregado que representa el pago de una reserva. Hay como máximo un pago por {@code bookingId}.
 */
@Entity
@Table(schema = "ps", name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "booking_id", nullable = false, unique = true)
  private UUID bookingId;

  @Column(name = "passenger_id", nullable = false)
  private UUID passengerId;

  @Column(name = "payment_method_id", nullable = false)
  private UUID paymentMethodId;

  @Embedded
  @AttributeOverride(
      name = "amount",
      column = @Column(name = "authorized_amount", precision = 12, scale = 2, nullable = false))
  @AttributeOverride(
      name = "currency",
      column = @Column(name = "currency", length = 3, nullable = false))
  private Money authorizedAmount;

  @Embedded
  @AttributeOverride(
      name = "amount",
      column = @Column(name = "captured_amount", precision = 12, scale = 2))
  @AttributeOverride(name = "currency", column = @Column(name = "captured_currency", length = 3))
  private Money capturedAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentStatus status;

  @Column(name = "provider_ref", length = 100)
  private String providerRef;

  @Enumerated(EnumType.STRING)
  @Column(name = "cancellation_reason", length = 30)
  private CancellationReason cancellationReason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "authorized_at")
  private Instant authorizedAt;

  @Column(name = "captured_at")
  private Instant capturedAt;

  @Column(name = "voided_at")
  private Instant voidedAt;

  protected Payment() {}

  /** Crea un pago PENDING para la reserva antes de consultar a la pasarela. */
  public static Payment create(
      UUID bookingId, UUID passengerId, UUID paymentMethodId, Money amount) {
    if (!amount.isPositive()) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
    Payment payment = new Payment();
    payment.bookingId = bookingId;
    payment.passengerId = passengerId;
    payment.paymentMethodId = paymentMethodId;
    payment.authorizedAmount = amount;
    payment.status = PaymentStatus.PENDING;
    payment.createdAt = Instant.now();
    return payment;
  }

  /** Prepara un nuevo intento sobre un pago REJECTED de la misma reserva y del mismo pasajero. */
  public void retry(UUID passengerId, UUID paymentMethodId, Money amount) {
    requireStatus(PaymentStatus.REJECTED);
    if (!this.passengerId.equals(passengerId)) {
      throw new ConflictException("Booking already has a payment from another passenger");
    }
    if (!amount.isPositive()) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
    this.paymentMethodId = paymentMethodId;
    this.authorizedAmount = amount;
    this.status = PaymentStatus.PENDING;
  }

  /** Marca el pago como AUTHORIZED con la referencia devuelta por la pasarela. */
  public void markAuthorized(String providerRef, Instant at) {
    requireStatus(PaymentStatus.PENDING);
    this.providerRef = providerRef;
    this.status = PaymentStatus.AUTHORIZED;
    this.authorizedAt = at;
  }

  /** Marca el pago como REJECTED tras la negativa de la pasarela. */
  public void markRejected() {
    requireStatus(PaymentStatus.PENDING);
    this.status = PaymentStatus.REJECTED;
  }

  /**
   * Captura el monto final de un pago AUTHORIZED; nunca puede superar lo autorizado.
   *
   * @throws ConflictException si el pago no está AUTHORIZED o el monto excede lo autorizado
   */
  public void capture(Money amount, Instant at) {
    requireStatus(PaymentStatus.AUTHORIZED);
    if (!amount.isPositive()) {
      throw new IllegalArgumentException("Captured amount must be greater than zero");
    }
    if (amount.isGreaterThan(authorizedAmount)) {
      throw new ConflictException(
          "Captured amount exceeds the authorized amount for booking " + bookingId);
    }
    this.capturedAmount = amount;
    this.status = PaymentStatus.CAPTURED;
    this.capturedAt = at;
  }

  /** Cobra solo el monto de la penalidad sobre un pago AUTHORIZED de una reserva cancelada. */
  public void chargePenalty(Money penalty, CancellationReason reason, Instant at) {
    capture(penalty, at);
    this.cancellationReason = reason;
  }

  /**
   * Anula la autorización de una reserva cancelada sin penalidad.
   *
   * @throws ConflictException si el pago no está AUTHORIZED
   */
  public void voidAuthorization(CancellationReason reason, Instant at) {
    requireStatus(PaymentStatus.AUTHORIZED);
    this.status = PaymentStatus.VOIDED;
    this.cancellationReason = reason;
    this.voidedAt = at;
  }

  /**
   * Indica si el pago ya fue resuelto por una cancelación, lo que hace idempotente un nuevo aviso.
   */
  public boolean isCancellationResolved() {
    return cancellationReason != null;
  }

  /**
   * Indica si la reserva ya tiene un cobro vigente, lo que hace idempotente una nueva autorización.
   */
  public boolean isAuthorizedOrCaptured() {
    return status == PaymentStatus.AUTHORIZED || status == PaymentStatus.CAPTURED;
  }

  private void requireStatus(PaymentStatus expected) {
    if (status != expected) {
      throw new ConflictException("Payment must be " + expected + " but is " + status);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getBookingId() {
    return bookingId;
  }

  public UUID getPassengerId() {
    return passengerId;
  }

  public UUID getPaymentMethodId() {
    return paymentMethodId;
  }

  public Money getAuthorizedAmount() {
    return authorizedAmount;
  }

  public Money getCapturedAmount() {
    return capturedAmount;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getProviderRef() {
    return providerRef;
  }

  public CancellationReason getCancellationReason() {
    return cancellationReason;
  }

  public Instant getAuthorizedAt() {
    return authorizedAt;
  }

  public Instant getCapturedAt() {
    return capturedAt;
  }

  public Instant getVoidedAt() {
    return voidedAt;
  }
}
