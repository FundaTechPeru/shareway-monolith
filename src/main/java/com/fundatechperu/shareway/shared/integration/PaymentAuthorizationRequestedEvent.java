package com.fundatechperu.shareway.shared.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Booking &amp; Pricing held a seat and asks Payments &amp; Settlement to authorize the estimated
 * fare of the booking.
 */
public record PaymentAuthorizationRequestedEvent(
    UUID bookingId, UUID passengerId, BigDecimal amount, String currency, Instant occurredAt) {}
