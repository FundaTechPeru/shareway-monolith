package com.fundatechperu.shareway.booking.pricing.infrastructure.persistence.repository;

import com.fundatechperu.shareway.booking.pricing.infrastructure.persistence.entity.FareRuleJpaEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFareRuleRepository extends JpaRepository<FareRuleJpaEntity, UUID> {

  // A derived query consumes one argument per comparison (valid_from <= ? AND valid_to >= ?),
  // so the caller passes the same day as both bounds: the rule whose range includes that day.
  Optional<FareRuleJpaEntity>
      findFirstByValidFromLessThanEqualAndValidToGreaterThanEqualOrderByVersionDesc(
          LocalDate from, LocalDate to);

  List<FareRuleJpaEntity> findAllByOrderByValidFromDesc();
}
