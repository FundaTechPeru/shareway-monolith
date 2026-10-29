package com.fundatechperu.shareway.payments.application;

import com.fundatechperu.shareway.payments.domain.event.DriverSettlementCreated;
import com.fundatechperu.shareway.payments.domain.event.PaymentCaptured;
import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.Money;
import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.domain.policy.CommissionPolicy;
import com.fundatechperu.shareway.payments.domain.repository.DriverSettlementRepository;
import com.fundatechperu.shareway.payments.domain.repository.PaymentGateway;
import com.fundatechperu.shareway.payments.domain.repository.PaymentRepository;
import com.fundatechperu.shareway.payments.infrastructure.messaging.DomainEventPublisher;
import com.fundatechperu.shareway.shared.exception.ConflictException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cobra las reservas de un viaje completado y genera la liquidación del conductor (US25). */
@Service
public class PaymentCaptureService {

  private final PaymentRepository payments;
  private final DriverSettlementRepository settlements;
  private final PaymentGateway gateway;
  private final CommissionPolicy commissionPolicy;
  private final DomainEventPublisher events;

  public PaymentCaptureService(
      PaymentRepository payments,
      DriverSettlementRepository settlements,
      PaymentGateway gateway,
      CommissionPolicy commissionPolicy,
      DomainEventPublisher events) {
    this.payments = payments;
    this.settlements = settlements;
    this.gateway = gateway;
    this.commissionPolicy = commissionPolicy;
    this.events = events;
  }

  /** Monto final a cobrar por una reserva del viaje. */
  public record TripCharge(UUID bookingId, BigDecimal finalAmount) {}

  /**
   * Captura todos los pagos del viaje y crea la liquidación PENDING en una sola transacción (todo o
   * nada). Es idempotente por {@code tripId}: si ya existe la liquidación se devuelve sin volver a
   * capturar.
   *
   * @throws ConflictException si alguna reserva no tiene pago AUTHORIZED o su monto final excede lo
   *     autorizado
   */
  @Transactional
  public IdempotentResult<DriverSettlement> captureTrip(
      UUID tripId, UUID driverId, Instant completedAt, List<TripCharge> charges) {
    Optional<DriverSettlement> existing = settlements.findByTripId(tripId);
    if (existing.isPresent()) {
      return IdempotentResult.existing(existing.get());
    }
    Instant now = Instant.now();
    Money gross = Money.zero(Money.PEN);
    List<Payment> captured = new ArrayList<>();
    for (TripCharge charge : charges) {
      Payment payment =
          payments
              .findByBookingId(charge.bookingId())
              .orElseThrow(
                  () ->
                      new ConflictException(
                          "Booking " + charge.bookingId() + " has no authorized payment"));
      Money finalAmount =
          Money.of(charge.finalAmount(), payment.getAuthorizedAmount().getCurrency());
      payment.capture(finalAmount, now);
      gross = gross.add(finalAmount);
      captured.add(payment);
    }
    for (Payment payment : captured) {
      gateway.capture(payment.getProviderRef(), payment.getCapturedAmount());
      payments.save(payment);
      events.publish(
          new PaymentCaptured(
              payment.getId(),
              payment.getBookingId(),
              payment.getCapturedAmount().getAmount(),
              payment.getCapturedAmount().getCurrency(),
              now));
    }
    DriverSettlement settlement =
        settlements.save(
            DriverSettlement.create(tripId, driverId, gross, completedAt, commissionPolicy));
    events.publish(
        new DriverSettlementCreated(
            settlement.getId(),
            tripId,
            driverId,
            settlement.getGross().getAmount(),
            settlement.getCommission().getAmount(),
            settlement.getNet().getAmount(),
            settlement.getGross().getCurrency(),
            now));
    return IdempotentResult.created(settlement);
  }
}
