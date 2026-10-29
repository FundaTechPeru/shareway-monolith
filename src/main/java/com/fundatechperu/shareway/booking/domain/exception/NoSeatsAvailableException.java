package com.fundatechperu.shareway.booking.domain.exception;

import com.fundatechperu.shareway.shared.exception.ConflictException;

public class NoSeatsAvailableException extends ConflictException {
  public NoSeatsAvailableException() {
    super("El viaje ya no tiene asientos disponibles.");
  }
}
