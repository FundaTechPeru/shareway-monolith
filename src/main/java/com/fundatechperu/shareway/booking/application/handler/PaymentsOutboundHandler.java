package com.fundatechperu.shareway.booking.application.handler;

import com.fundatechperu.shareway.booking.domain.service.DepartureTimeProvider;
import com.fundatechperu.shareway.booking.domain.service.PaymentsPort;
import com.fundatechperu.shareway.booking.pricing.domain.model.Fare;
import com.fundatechperu.shareway.booking.pricing.domain.repository.FareRepository;
import com.fundatechperu.shareway.booking.shared.events.BookingCancelled;
import com.fundatechperu.shareway.booking.shared.events.EventHandler;
import com.fundatechperu.shareway.booking.shared.events.PaymentAuthorizationRequested;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Forwards the booking events that Payments &amp; Settlement needs through the {@link
 * PaymentsPort}: the authorization of the estimated fare when a seat is held, and every
 * cancellation with the scheduled departure of its group.
 */
@Service
public class PaymentsOutboundHandler implements EventHandler {

  private static final Logger log = LoggerFactory.getLogger(PaymentsOutboundHandler.class);

  private final PaymentsPort paymentsPort;
  private final FareRepository fareRepository;
  private final DepartureTimeProvider departureTimeProvider;

  public PaymentsOutboundHandler(
      PaymentsPort paymentsPort,
      FareRepository fareRepository,
      DepartureTimeProvider departureTimeProvider) {
    this.paymentsPort = paymentsPort;
    this.fareRepository = fareRepository;
    this.departureTimeProvider = departureTimeProvider;
  }

  @Override
  public boolean supports(Object event) {
    return event instanceof PaymentAuthorizationRequested || event instanceof BookingCancelled;
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof PaymentAuthorizationRequested requested) {
      requestAuthorization(requested);
    } else if (event instanceof BookingCancelled cancelled) {
      paymentsPort.notifyCancellation(
          cancelled.bookingId(),
          cancelled.payerId(),
          cancelled.reason(),
          cancelled.occurredAt(),
          departureTimeProvider.departureAt(cancelled.groupId()));
    }
  }

  private void requestAuthorization(PaymentAuthorizationRequested requested) {
    fareRepository
        .findByBookingIdAndFareType(requested.bookingId(), Fare.ESTIMATED)
        .ifPresentOrElse(
            fare ->
                paymentsPort.requestAuthorization(
                    requested.bookingId(), requested.payerId(), fare.amount(), fare.currency()),
            () ->
                log.warn(
                    "Booking {} has no estimated fare; payment authorization not requested",
                    requested.bookingId()));
  }
}
