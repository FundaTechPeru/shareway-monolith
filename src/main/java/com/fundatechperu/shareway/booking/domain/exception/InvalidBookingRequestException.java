package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.DomainValidationException;

public class InvalidBookingRequestException extends DomainValidationException {

  public InvalidBookingRequestException(String message) {
    super(message);
  }
}
