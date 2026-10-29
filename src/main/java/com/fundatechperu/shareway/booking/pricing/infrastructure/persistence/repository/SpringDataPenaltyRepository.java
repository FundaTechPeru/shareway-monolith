package com.fundatechperu.shareway.booking.pricing.infrastructure.persistence.repository;

import com.fundatechperu.shareway.booking.pricing.infrastructure.persistence.entity.PenaltyJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPenaltyRepository extends JpaRepository<PenaltyJpaEntity, UUID> {
  List<PenaltyJpaEntity> findByBookingId(UUID bookingId);
}
