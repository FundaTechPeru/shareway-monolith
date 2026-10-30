package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.ConflictException;

public class BookingAlreadyExistsException extends ConflictException {

  public BookingAlreadyExistsException(String message) {
    super(message);
  }
}
