package com.fundatechperu.shareway.payments.infrastructure.persistence;

import com.fundatechperu.shareway.payments.domain.model.Payment;
import com.fundatechperu.shareway.payments.domain.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Adaptador JPA del puerto {@link PaymentRepository}. */
@Repository
public class PaymentPersistenceAdapter implements PaymentRepository {

  private final JpaPaymentRepository jpa;

  public PaymentPersistenceAdapter(JpaPaymentRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Payment save(Payment payment) {
    return jpa.save(payment);
  }

  @Override
  public Optional<Payment> findByBookingId(UUID bookingId) {
    return jpa.findByBookingId(bookingId);
  }

  @Override
  public Optional<Payment> findByIdAndPassengerId(UUID id, UUID passengerId) {
    return jpa.findByIdAndPassengerId(id, passengerId);
  }

  @Override
  public List<Payment> findByPassengerId(UUID passengerId) {
    return jpa.findByPassengerIdOrderByCreatedAtDesc(passengerId);
  }
}
