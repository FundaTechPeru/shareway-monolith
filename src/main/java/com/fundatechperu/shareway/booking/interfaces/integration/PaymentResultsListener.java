package com.fundatechperu.shareway.booking.interfaces.integration;

import com.fundatechperu.shareway.booking.shared.events.EventPublisher;
import com.fundatechperu.shareway.booking.shared.events.PaymentAuthorizationFailed;
import com.fundatechperu.shareway.booking.shared.events.PaymentAuthorized;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationFailedEvent;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationSucceededEvent;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Feeds the authorization results of Payments &amp; Settlement into the booking saga, once the
 * payment transaction commits. Each result is handled in a new transaction of its own, because the
 * one that published it is already committed.
 */
@Component
public class PaymentResultsListener {

  private final EventPublisher eventPublisher;
  private final TransactionTemplate newTransaction;
  private final Clock clock;

  public PaymentResultsListener(
      EventPublisher eventPublisher, PlatformTransactionManager transactionManager, Clock clock) {
    this.eventPublisher = eventPublisher;
    this.newTransaction = new TransactionTemplate(transactionManager);
    this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.clock = clock;
  }

  @TransactionalEventListener(fallbackExecution = true)
  public void onAuthorized(PaymentAuthorizationSucceededEvent event) {
    newTransaction.executeWithoutResult(
        status ->
            eventPublisher.publish(
                new PaymentAuthorized(
                    UUID.randomUUID(), clock.instant(), event.bookingId(), event.paymentId())));
  }

  @TransactionalEventListener(fallbackExecution = true)
  public void onFailed(PaymentAuthorizationFailedEvent event) {
    newTransaction.executeWithoutResult(
        status ->
            eventPublisher.publish(
                new PaymentAuthorizationFailed(
                    UUID.randomUUID(), clock.instant(), event.bookingId(), event.reason())));
  }
}
