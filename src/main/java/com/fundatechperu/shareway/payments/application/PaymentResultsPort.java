package com.fundatechperu.shareway.payments.application;

import java.util.UUID;

/**
 * Port to report to Booking &amp; Pricing the result of an authorization it requested
 * asynchronously. An extracted Payments &amp; Settlement answers the internal authorization
 * endpoint instead, so this port only exists while both contexts share the monolith.
 */
public interface PaymentResultsPort {

  void authorized(UUID bookingId, UUID paymentId);

  void rejected(UUID bookingId, String reason);
}
