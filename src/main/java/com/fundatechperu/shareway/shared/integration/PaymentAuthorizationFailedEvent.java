package com.fundatechperu.shareway.shared.integration;

import java.time.Instant;
import java.util.UUID;

/**
 * Payments &amp; Settlement could not authorize the payment of a booking: no active card, card
 * declined or gateway unavailable.
 */
public record PaymentAuthorizationFailedEvent(UUID bookingId, String reason, Instant occurredAt) {}
