package com.fundatechperu.shareway.booking.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbound port to Payments &amp; Settlement. Both calls are fire-and-forget: the authorization
 * result comes back later as a PaymentAuthorized or PaymentAuthorizationFailed event for the
 * booking saga.
 *
 * <p>Payments &amp; Settlement runs as its own service: the REST adapter calls its
 * /api/v1/internal endpoints.
 */
public interface PaymentsPort {

  /** Asks to authorize the estimated fare of a booking on one of the passenger's cards. */
  void requestAuthorization(UUID bookingId, UUID passengerId, BigDecimal amount, String currency);

  /** Reports a cancelled booking so its payment is penalized or its authorization voided. */
  void notifyCancellation(
      UUID bookingId,
      UUID passengerId,
      String reason,
      Instant cancelledAt,
      Instant scheduledDepartureAt);
}
