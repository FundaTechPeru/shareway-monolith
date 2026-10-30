package com.fundatechperu.shareway.booking.pricing.domain.exception;

import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;

public class FareNotFoundException extends ResourceNotFoundException {

  public FareNotFoundException(UUID fareId) {
    super("Fare not found: " + fareId);
  }

  public FareNotFoundException(String message) {
    super(message);
  }
}
