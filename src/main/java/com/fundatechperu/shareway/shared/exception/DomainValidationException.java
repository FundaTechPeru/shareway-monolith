package com.fundatechperu.shareway.shared.exception;

import java.util.Optional;

/**
 * The input passed Bean Validation but breaks a domain rule; maps to 400, optionally reporting the
 * offending field.
 */
public class DomainValidationException extends RuntimeException {

  private final String field;

  public DomainValidationException(String message) {
    this(null, message);
  }

  public DomainValidationException(String field, String message) {
    super(message);
    this.field = field;
  }

  public Optional<String> getField() {
    return Optional.ofNullable(field);
  }
}
