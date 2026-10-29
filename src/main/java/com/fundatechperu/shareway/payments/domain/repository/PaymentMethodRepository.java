package com.fundatechperu.shareway.payments.domain.repository;

import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de los métodos de pago. */
public interface PaymentMethodRepository {

  PaymentMethod save(PaymentMethod paymentMethod);

  Optional<PaymentMethod> findById(UUID id);

  List<PaymentMethod> findActiveByPassengerId(UUID passengerId);

  boolean existsActiveByPassengerIdAndCardToken(UUID passengerId, String cardToken);
}
