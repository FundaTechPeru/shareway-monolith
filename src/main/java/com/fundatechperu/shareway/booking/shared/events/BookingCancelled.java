package com.fundatechperu.shareway.booking.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Published when a booking is cancelled, by its passenger or by the saga compensation.
 *
 * @param reason why the booking was cancelled: {@link #PASSENGER_CANCELLED}, {@link
 *     #PAYMENT_FAILED}, {@link #PAYMENT_TIMEOUT} or {@link #NO_DRIVER_AVAILABLE}
 */
public record BookingCancelled(
    UUID eventId,
    Instant occurredAt,
    UUID bookingId,
    UUID groupId,
    UUID payerId,
    boolean penaltyApplicable,
    String reason) {

  public static final String PASSENGER_CANCELLED = "PASSENGER_CANCELLED";
  public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
  public static final String PAYMENT_TIMEOUT = "PAYMENT_TIMEOUT";
  public static final String NO_DRIVER_AVAILABLE = "NO_DRIVER_AVAILABLE";
}
