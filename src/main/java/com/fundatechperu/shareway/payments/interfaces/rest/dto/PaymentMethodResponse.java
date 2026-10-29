package com.fundatechperu.shareway.payments.interfaces.rest.dto;

import com.fundatechperu.shareway.payments.domain.model.CardBrand;
import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import java.util.UUID;

/** Vista pública de una tarjeta; omite el token de la pasarela. */
public record PaymentMethodResponse(
    UUID id, CardBrand brand, String last4, int expMonth, int expYear) {

  public static PaymentMethodResponse from(PaymentMethod method) {
    return new PaymentMethodResponse(
        method.getId(),
        method.getBrand(),
        method.getLast4(),
        method.getExpMonth(),
        method.getExpYear());
  }
}
