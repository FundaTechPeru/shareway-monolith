package com.fundatechperu.shareway.payments.domain.exception;

/** Indica que la pasarela de pagos rechazó la transacción. */
public class PaymentDeclinedException extends RuntimeException {

  public PaymentDeclinedException(String message) {
    super(message);
  }
}
