package com.fundatechperu.shareway.booking.domain.service;

import java.util.UUID;

/**
 * Outbound port to Trip Planning &amp; Matching: the {@code requestId} of a booking is a trip
 * request of that context, and only the passenger who asked for it can book with it.
 *
 * <p>Inside the monolith it is answered in process; when Trip Planning &amp; Matching is extracted,
 * a REST adapter asks the service instead.
 */
public interface TripRequestsPort {

  /**
   * Tells whether the trip request exists and belongs to the passenger.
   *
   * @throws com.fundatechperu.shareway.shared.exception.ServiceUnavailableException when Trip
   *     Planning &amp; Matching cannot answer
   */
  boolean belongsTo(UUID tripRequestId, UUID passengerId);
}
