package com.fundatechperu.shareway.payments.infrastructure.integration;

import com.fundatechperu.shareway.payments.application.PaymentResultsPort;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationFailedEvent;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationSucceededEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/** Reports authorization results as integration events consumed by Booking &amp; Pricing. */
@Component
public class InProcessPaymentResultsAdapter implements PaymentResultsPort {

  private final ApplicationEventPublisher publisher;

  public InProcessPaymentResultsAdapter(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void authorized(UUID bookingId, UUID paymentId) {
    publisher.publishEvent(
        new PaymentAuthorizationSucceededEvent(bookingId, paymentId, Instant.now()));
  }

  @Override
  public void rejected(UUID bookingId, String reason) {
    publisher.publishEvent(new PaymentAuthorizationFailedEvent(bookingId, reason, Instant.now()));
  }
}
