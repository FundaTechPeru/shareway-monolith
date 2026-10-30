package com.fundatechperu.shareway.shared.integration;

import java.time.Instant;
import java.util.UUID;

/** Payments &amp; Settlement authorized the payment of a booking. */
public record PaymentAuthorizationSucceededEvent(
    UUID bookingId, UUID paymentId, Instant occurredAt) {}
