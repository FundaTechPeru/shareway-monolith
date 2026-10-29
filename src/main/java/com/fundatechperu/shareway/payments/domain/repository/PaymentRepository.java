package com.fundatechperu.shareway.payments.domain.repository;

import com.fundatechperu.shareway.payments.domain.model.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de los pagos. */
public interface PaymentRepository {

  Payment save(Payment payment);

  Optional<Payment> findByBookingId(UUID bookingId);

  Optional<Payment> findByIdAndPassengerId(UUID id, UUID passengerId);

  List<Payment> findByPassengerId(UUID passengerId);
}
