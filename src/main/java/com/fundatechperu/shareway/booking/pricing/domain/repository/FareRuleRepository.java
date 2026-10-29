package com.fundatechperu.shareway.booking.pricing.domain.repository;

import com.fundatechperu.shareway.booking.pricing.domain.model.FareRule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FareRuleRepository {
  Optional<FareRule> findById(UUID id);

  List<FareRule> findAll();

  Optional<FareRule> findActive(LocalDate date);
}
