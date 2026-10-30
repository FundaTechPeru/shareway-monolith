package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;

/** The trip request of a booking does not exist or belongs to another passenger. */
public class TripRequestNotFoundException extends ResourceNotFoundException {
  public TripRequestNotFoundException(UUID requestId) {
    super("Trip request not found: " + requestId);
  }
}
