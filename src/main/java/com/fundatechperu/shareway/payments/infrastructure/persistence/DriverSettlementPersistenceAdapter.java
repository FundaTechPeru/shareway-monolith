package com.fundatechperu.shareway.payments.infrastructure.persistence;

import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import com.fundatechperu.shareway.payments.domain.repository.DriverSettlementRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Adaptador JPA del puerto {@link DriverSettlementRepository}. */
@Repository
public class DriverSettlementPersistenceAdapter implements DriverSettlementRepository {

  private final JpaDriverSettlementRepository jpa;

  public DriverSettlementPersistenceAdapter(JpaDriverSettlementRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public DriverSettlement save(DriverSettlement settlement) {
    return jpa.save(settlement);
  }

  @Override
  public Optional<DriverSettlement> findById(UUID id) {
    return jpa.findById(id);
  }

  @Override
  public Optional<DriverSettlement> findByTripId(UUID tripId) {
    return jpa.findByTripId(tripId);
  }

  @Override
  public Optional<DriverSettlement> findByIdAndDriverId(UUID id, UUID driverId) {
    return jpa.findByIdAndDriverId(id, driverId);
  }

  @Override
  public List<DriverSettlement> findByDriverId(UUID driverId) {
    return jpa.findByDriverIdOrderByCreatedAtDesc(driverId);
  }

  @Override
  public List<DriverSettlement> findAll(Optional<SettlementStatus> status) {
    return status
        .map(jpa::findByStatusOrderByCreatedAtDesc)
        .orElseGet(jpa::findAllByOrderByCreatedAtDesc);
  }
}
