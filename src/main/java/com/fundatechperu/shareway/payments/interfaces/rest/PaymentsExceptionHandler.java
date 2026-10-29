package com.fundatechperu.shareway.payments.interfaces.rest;

import com.fundatechperu.shareway.payments.domain.exception.GatewayUnavailableException;
import com.fundatechperu.shareway.payments.domain.exception.PaymentDeclinedException;
import com.fundatechperu.shareway.shared.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Statuses that only Payments & Settlement uses: 402 for a declined card and 503 when the payment
 * gateway is down. Every other error goes through the shared handler.
 */
@RestControllerAdvice(basePackages = "com.fundatechperu.shareway.payments")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentsExceptionHandler {

  @ExceptionHandler(PaymentDeclinedException.class)
  public ResponseEntity<ErrorResponse> handleDeclined(
      PaymentDeclinedException ex, HttpServletRequest request) {
    return build(HttpStatus.PAYMENT_REQUIRED, ex.getMessage(), request);
  }

  @ExceptionHandler(GatewayUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleGatewayUnavailable(
      GatewayUnavailableException ex, HttpServletRequest request) {
    return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
  }

  private ResponseEntity<ErrorResponse> build(
      HttpStatus status, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status, message, request.getRequestURI()));
  }
}
