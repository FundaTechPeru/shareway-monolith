package com.fundatechperu.shareway.payments.infrastructure.persistence;

import com.fundatechperu.shareway.payments.domain.model.Penalty;
import com.fundatechperu.shareway.payments.domain.repository.PenaltyRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Adaptador JPA del puerto {@link PenaltyRepository}. */
@Repository
public class PenaltyPersistenceAdapter implements PenaltyRepository {

  private final JpaPenaltyRepository jpa;

  public PenaltyPersistenceAdapter(JpaPenaltyRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Penalty save(Penalty penalty) {
    return jpa.save(penalty);
  }

  @Override
  public Optional<Penalty> findByBookingId(UUID bookingId) {
    return jpa.findByBookingId(bookingId);
  }
}
