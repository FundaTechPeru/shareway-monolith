package com.fundatechperu.shareway.payments.domain.model;

/** Estado de la liquidación al conductor: PENDING hasta que el administrador registra el pago. */
public enum SettlementStatus {
  PENDING,
  PAID
}
