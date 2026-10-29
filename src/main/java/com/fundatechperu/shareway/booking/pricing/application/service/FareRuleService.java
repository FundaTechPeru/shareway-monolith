package com.fundatechperu.shareway.booking.pricing.application.service;

import com.fundatechperu.shareway.booking.pricing.domain.exception.FareNotFoundException;
import com.fundatechperu.shareway.booking.pricing.domain.model.FareRule;
import com.fundatechperu.shareway.booking.pricing.domain.repository.FareRuleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FareRuleService {
  private final FareRuleRepository repository;

  public FareRuleService(FareRuleRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public FareRule findById(UUID id) {
    return repository.findById(id).orElseThrow(() -> new FareNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public List<FareRule> findAll() {
    return repository.findAll();
  }
}
