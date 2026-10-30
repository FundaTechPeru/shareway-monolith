package com.fundatechperu.shareway.booking.pricing.domain.exception;

import com.fundatechperu.shareway.shared.exception.ConflictException;

public class InvalidFareException extends ConflictException {

  public InvalidFareException(String message) {
    super(message);
  }
}
