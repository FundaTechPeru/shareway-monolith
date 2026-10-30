package com.fundatechperu.shareway.shared.exception;

/** The requested resource does not exist or does not belong to the current user; maps to 404. */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
