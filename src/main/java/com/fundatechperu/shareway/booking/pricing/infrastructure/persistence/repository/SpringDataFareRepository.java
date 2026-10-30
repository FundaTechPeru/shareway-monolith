package com.fundatechperu.shareway.booking.pricing.infrastructure.persistence.repository;

import com.fundatechperu.shareway.booking.pricing.infrastructure.persistence.entity.FareJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFareRepository extends JpaRepository<FareJpaEntity, UUID> {
  Optional<FareJpaEntity> findByBookingIdAndFareType(UUID bookingId, String fareType);
}
