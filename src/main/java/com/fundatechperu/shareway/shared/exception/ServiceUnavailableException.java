package com.fundatechperu.shareway.shared.exception;

/** A service the request depends on did not answer; maps to 503. */
public class ServiceUnavailableException extends RuntimeException {
  public ServiceUnavailableException(String message) {
    super(message);
  }
}
