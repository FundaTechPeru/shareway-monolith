package com.fundatechperu.shareway.payments.domain.repository;

import com.fundatechperu.shareway.payments.domain.model.Penalty;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de las penalidades. */
public interface PenaltyRepository {

  Penalty save(Penalty penalty);

  Optional<Penalty> findByBookingId(UUID bookingId);
}
