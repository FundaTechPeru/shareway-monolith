package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.booking.domain.model.BookingStatus;
import com.fundatechperu.shareway.shared.exception.ConflictException;

public class InvalidBookingTransitionException extends ConflictException {
  public InvalidBookingTransitionException(BookingStatus current, BookingStatus target) {
    super("Invalid booking transition from " + current + " to " + target);
  }
}
