package com.fundatechperu.shareway.payments.domain.repository;

import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de persistencia de las liquidaciones a conductores. */
public interface DriverSettlementRepository {

  DriverSettlement save(DriverSettlement settlement);

  Optional<DriverSettlement> findById(UUID id);

  Optional<DriverSettlement> findByTripId(UUID tripId);

  Optional<DriverSettlement> findByIdAndDriverId(UUID id, UUID driverId);

  List<DriverSettlement> findByDriverId(UUID driverId);

  List<DriverSettlement> findAll(Optional<SettlementStatus> status);
}
