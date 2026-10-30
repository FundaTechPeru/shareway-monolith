package com.fundatechperu.shareway.booking.infrastructure.integration;

import com.fundatechperu.shareway.booking.domain.service.PaymentsPort;
import com.fundatechperu.shareway.shared.integration.BookingCancelledEvent;
import com.fundatechperu.shareway.shared.integration.PaymentAuthorizationRequestedEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * {@link PaymentsPort} for the monolith: publishes integration events that Payments &amp;
 * Settlement consumes once the booking transaction commits. {@link HttpPaymentsAdapter} takes its
 * place when {@code app.services.payments-settlement.url} is set.
 */
@Component
@ConditionalOnExpression("'${app.services.payments-settlement.url:}'.isBlank()")
public class InProcessPaymentsAdapter implements PaymentsPort {

  private final ApplicationEventPublisher publisher;
  private final Clock clock;

  public InProcessPaymentsAdapter(ApplicationEventPublisher publisher, Clock clock) {
    this.publisher = publisher;
    this.clock = clock;
  }

  @Override
  public void requestAuthorization(
      UUID bookingId, UUID passengerId, BigDecimal amount, String currency) {
    publisher.publishEvent(
        new PaymentAuthorizationRequestedEvent(
            bookingId, passengerId, amount, currency, clock.instant()));
  }

  @Override
  public void notifyCancellation(
      UUID bookingId,
      UUID passengerId,
      String reason,
      Instant cancelledAt,
      Instant scheduledDepartureAt) {
    publisher.publishEvent(
        new BookingCancelledEvent(
            bookingId, passengerId, reason, cancelledAt, scheduledDepartureAt));
  }
}
