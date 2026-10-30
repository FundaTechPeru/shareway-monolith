package com.fundatechperu.shareway.shared.integration;

import java.time.Instant;
import java.util.UUID;

/**
 * Booking &amp; Pricing cancelled a booking. Payments &amp; Settlement charges the cancellation
 * penalty or voids the authorization.
 *
 * @param reason PASSENGER_CANCELLED, NO_SHOW, or a platform reason such as NO_DRIVER_AVAILABLE,
 *     PAYMENT_TIMEOUT or PAYMENT_FAILED, which are never penalized
 */
public record BookingCancelledEvent(
    UUID bookingId,
    UUID passengerId,
    String reason,
    Instant cancelledAt,
    Instant scheduledDepartureAt) {}
