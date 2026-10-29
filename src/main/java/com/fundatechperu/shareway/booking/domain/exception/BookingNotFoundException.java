package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;

public class BookingNotFoundException extends ResourceNotFoundException {
  public BookingNotFoundException(UUID bookingId) {
    super("Booking not found: " + bookingId);
  }
}
