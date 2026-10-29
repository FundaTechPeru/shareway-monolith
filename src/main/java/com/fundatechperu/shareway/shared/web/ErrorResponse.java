package com.fundatechperu.shareway.shared.web;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Body of every error response of the monolith.
 *
 * @param timestamp when the error happened
 * @param status HTTP status code
 * @param error HTTP reason phrase
 * @param message human readable description of the problem
 * @param path request path
 * @param fieldErrors invalid fields, empty when the error is not about the request content
 */
public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldErrorDetail> fieldErrors) {

  /** One invalid field of the request. */
  public record FieldErrorDetail(String field, String message) {}

  public static ErrorResponse of(HttpStatus status, String message, String path) {
    return of(status, message, path, List.of());
  }

  public static ErrorResponse of(
      HttpStatus status, String message, String path, List<FieldErrorDetail> fieldErrors) {
    return new ErrorResponse(
        Instant.now(), status.value(), status.getReasonPhrase(), message, path, fieldErrors);
  }
}
