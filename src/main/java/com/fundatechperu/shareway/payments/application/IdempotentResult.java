package com.fundatechperu.shareway.payments.application;

/**
 * Resultado de un caso de uso idempotente: {@code created} es falso cuando se devolvió un registro
 * ya existente.
 */
public record IdempotentResult<T>(T value, boolean created) {

  public static <T> IdempotentResult<T> created(T value) {
    return new IdempotentResult<>(value, true);
  }

  public static <T> IdempotentResult<T> existing(T value) {
    return new IdempotentResult<>(value, false);
  }
}
