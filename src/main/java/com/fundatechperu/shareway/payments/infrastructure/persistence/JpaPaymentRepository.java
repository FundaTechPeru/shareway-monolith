package com.fundatechperu.shareway.payments.infrastructure.persistence;

import com.fundatechperu.shareway.payments.domain.model.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de la tabla payments. */
public interface JpaPaymentRepository extends JpaRepository<Payment, UUID> {

  Optional<Payment> findByBookingId(UUID bookingId);

  Optional<Payment> findByIdAndPassengerId(UUID id, UUID passengerId);

  List<Payment> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId);
}
