package com.fundatechperu.shareway.payments.interfaces.integration;

import com.fundatechperu.shareway.payments.application.CancellationService;
import com.fundatechperu.shareway.payments.application.PaymentAuthorizationService;
import com.fundatechperu.shareway.payments.application.PaymentMethodService;
import com.fundatechperu.shareway.payments.application.PaymentResultsPort;
import com.fundatechperu.shareway.payments.domain.exception.PaymentDeclinedException;
import com.fundatechperu.shareway.payments.domain.model.CancellationReason;
import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import com.fundatechperu.shareway.shared.integration.BookingCancelledEvent;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationRequestedEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Recepción en proceso de los eventos de Booking &amp; Pricing, en lugar de los endpoints internos
 * que usaban los servicios separados. Cada evento se procesa en una transacción nueva una vez
 * confirmada la reserva, y el resultado de la autorización vuelve por el {@link
 * PaymentResultsPort}.
 */
@Component
public class BookingEventsListener {

  private static final Logger log = LoggerFactory.getLogger(BookingEventsListener.class);

  private final PaymentMethodService paymentMethodService;
  private final PaymentAuthorizationService authorizationService;
  private final CancellationService cancellationService;
  private final PaymentResultsPort results;
  private final TransactionTemplate newTransaction;

  public BookingEventsListener(
      PaymentMethodService paymentMethodService,
      PaymentAuthorizationService authorizationService,
      CancellationService cancellationService,
      PaymentResultsPort results,
      PlatformTransactionManager transactionManager) {
    this.paymentMethodService = paymentMethodService;
    this.authorizationService = authorizationService;
    this.cancellationService = cancellationService;
    this.results = results;
    this.newTransaction = new TransactionTemplate(transactionManager);
    this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * Autoriza la tarifa estimada con la primera tarjeta activa del pasajero. Un rechazo de la
   * pasarela queda guardado como REJECTED; cualquier fallo se informa a la reserva.
   */
  @TransactionalEventListener(fallbackExecution = true)
  public void onPaymentAuthorizationRequested(PaymentAuthorizationRequestedEvent event) {
    try {
      newTransaction.executeWithoutResult(status -> authorize(event));
    } catch (RuntimeException ex) {
      log.warn("Payment of booking {} not authorized: {}", event.bookingId(), ex.getMessage());
      newTransaction.executeWithoutResult(
          status -> results.rejected(event.bookingId(), ex.getMessage()));
    }
  }

  /** Cobra la penalidad o anula la autorización de una reserva cancelada. */
  @TransactionalEventListener(fallbackExecution = true)
  public void onBookingCancelled(BookingCancelledEvent event) {
    try {
      newTransaction.executeWithoutResult(
          status ->
              cancellationService.cancel(
                  event.bookingId(),
                  toCancellationReason(event.reason()),
                  event.cancelledAt(),
                  event.scheduledDepartureAt()));
    } catch (ResourceNotFoundException | ConflictException ex) {
      // A booking whose payment never got authorized has nothing to void or charge.
      log.info("Cancellation of booking {} ignored: {}", event.bookingId(), ex.getMessage());
    }
  }

  private void authorize(PaymentAuthorizationRequestedEvent event) {
    List<PaymentMethod> cards = paymentMethodService.listActive(event.passengerId());
    if (cards.isEmpty()) {
      results.rejected(event.bookingId(), "The passenger has no active payment method");
      return;
    }
    try {
      Payment payment =
          authorizationService
              .authorize(
                  event.bookingId(),
                  event.passengerId(),
                  cards.get(0).getId(),
                  event.amount(),
                  event.currency())
              .value();
      results.authorized(event.bookingId(), payment.getId());
    } catch (PaymentDeclinedException ex) {
      results.rejected(event.bookingId(), ex.getMessage());
    }
  }

  /**
   * Solo el pasajero genera penalidad; las cancelaciones de la plataforma (sin conductor, sin pago
   * a tiempo) anulan la autorización sin costo.
   */
  private static CancellationReason toCancellationReason(String reason) {
    return switch (reason) {
      case "PASSENGER_CANCELLED" -> CancellationReason.PASSENGER_CANCELLED;
      case "NO_SHOW" -> CancellationReason.NO_SHOW;
      default -> CancellationReason.NO_DRIVER_AVAILABLE;
    };
  }
}
