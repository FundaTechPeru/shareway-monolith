package com.fundatechperu.shareway.payments.infrastructure.persistence;

import com.fundatechperu.shareway.payments.domain.model.Penalty;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de la tabla penalties. */
public interface JpaPenaltyRepository extends JpaRepository<Penalty, UUID> {

  Optional<Penalty> findByBookingId(UUID bookingId);
}
