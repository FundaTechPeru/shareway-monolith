package com.fundatechperu.shareway.shared.integration;

import java.util.Optional;
import java.util.UUID;

/**
 * Query offered by Trip Planning &amp; Matching to the other contexts while it lives in the
 * monolith: who asked for a trip request. Once the context is extracted, callers ask the service
 * over HTTP instead.
 */
@FunctionalInterface
public interface TripRequestDirectory {

  /**
   * Returns the passenger of a trip request.
   *
   * @param tripRequestId id of the trip request
   * @return the passenger UUID, or empty when the trip request does not exist
   */
  Optional<UUID> passengerOf(UUID tripRequestId);
}
