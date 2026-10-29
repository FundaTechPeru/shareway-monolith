package com.fundatechperu.shareway.payments.domain.exception;

/** Indica que la pasarela de pagos no está disponible temporalmente. */
public class GatewayUnavailableException extends RuntimeException {

  public GatewayUnavailableException(String message) {
    super(message);
  }
}
