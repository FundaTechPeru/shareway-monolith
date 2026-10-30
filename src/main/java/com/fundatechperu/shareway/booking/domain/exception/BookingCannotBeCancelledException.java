package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.ConflictException;

/**
 * The booking exists but its status forbids the cancellation (boarded, in_progress or completed):
 * 409 Conflict with a clear message (Sprint 2, US-12).
 */
public class BookingCannotBeCancelledException extends ConflictException {

  public BookingCannotBeCancelledException(String message) {
    super(message);
  }
}
