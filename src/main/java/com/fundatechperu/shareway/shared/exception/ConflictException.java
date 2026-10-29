package com.fundatechperu.shareway.shared.exception;

/** The request conflicts with the current state, such as a duplicate; maps to 409. */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
