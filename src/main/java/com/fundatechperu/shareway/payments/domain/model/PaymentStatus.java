package com.fundatechperu.shareway.payments.domain.model;

/**
 * Ciclo de vida de un pago: PENDING mientras se consulta a la pasarela, luego AUTHORIZED o
 * REJECTED; un pago autorizado termina CAPTURED o VOIDED.
 */
public enum PaymentStatus {
  PENDING,
  AUTHORIZED,
  REJECTED,
  CAPTURED,
  VOIDED
}
