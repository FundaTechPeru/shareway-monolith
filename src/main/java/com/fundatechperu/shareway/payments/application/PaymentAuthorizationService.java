package com.fundatechperu.shareway.payments.application;

import com.fundatechperu.shareway.payments.domain.event.PaymentAuthorized;
import com.fundatechperu.shareway.payments.domain.event.PaymentRejected;
import com.fundatechperu.shareway.payments.domain.exception.PaymentDeclinedException;
import com.fundatechperu.shareway.payments.domain.model.Money;
import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import com.fundatechperu.shareway.payments.domain.repository.PaymentGateway;
import com.fundatechperu.shareway.payments.domain.repository.PaymentGateway.GatewayAuthorization;
import com.fundatechperu.shareway.payments.domain.repository.PaymentMethodRepository;
import com.fundatechperu.shareway.payments.domain.repository.PaymentRepository;
import com.fundatechperu.shareway.payments.infrastructure.messaging.DomainEventPublisher;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autoriza el pago de una reserva (US04). Lo invoca la Saga de Booking &amp; Pricing al reservar.
 */
@Service
public class PaymentAuthorizationService {

  private final PaymentRepository payments;
  private final PaymentMethodRepository paymentMethods;
  private final PaymentGateway gateway;
  private final DomainEventPublisher events;

  public PaymentAuthorizationService(
      PaymentRepository payments,
      PaymentMethodRepository paymentMethods,
      PaymentGateway gateway,
      DomainEventPublisher events) {
    this.payments = payments;
    this.paymentMethods = paymentMethods;
    this.gateway = gateway;
    this.events = events;
  }

  /**
   * Autoriza el monto de la reserva de forma idempotente por {@code bookingId}. Si la reserva ya
   * tiene un pago autorizado o capturado lo devuelve sin llamar a la pasarela; si estaba rechazado
   * reintenta sobre el mismo registro. Un rechazo queda guardado como REJECTED.
   *
   * @throws PaymentDeclinedException si la pasarela rechaza la tarjeta
   */
  @Transactional(noRollbackFor = PaymentDeclinedException.class)
  public IdempotentResult<Payment> authorize(
      UUID bookingId, UUID passengerId, UUID paymentMethodId, BigDecimal amount, String currency) {
    Money money = Money.of(amount, currency);
    Optional<Payment> existing = payments.findByBookingId(bookingId);
    if (existing.isPresent() && existing.get().isAuthorizedOrCaptured()) {
      return IdempotentResult.existing(existing.get());
    }
    PaymentMethod method =
        paymentMethods
            .findById(paymentMethodId)
            .filter(candidate -> candidate.isUsableBy(passengerId))
            .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
    Payment payment =
        existing
            .map(
                previous -> {
                  previous.retry(passengerId, paymentMethodId, money);
                  return previous;
                })
            .orElseGet(() -> Payment.create(bookingId, passengerId, paymentMethodId, money));

    GatewayAuthorization result = gateway.authorize(method.getCardToken(), money);
    Instant now = Instant.now();
    if (!result.approved()) {
      payment.markRejected();
      Payment rejected = payments.save(payment);
      events.publish(new PaymentRejected(rejected.getId(), bookingId, passengerId, now));
      throw new PaymentDeclinedException("Payment was declined by the gateway");
    }
    payment.markAuthorized(result.providerRef(), now);
    Payment authorized = payments.save(payment);
    events.publish(
        new PaymentAuthorized(
            authorized.getId(),
            bookingId,
            passengerId,
            money.getAmount(),
            money.getCurrency(),
            now));
    return IdempotentResult.created(authorized);
  }
}
