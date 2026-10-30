package com.fundatechperu.shareway.payments.infrastructure.persistence;

import com.fundatechperu.shareway.payments.domain.model.DriverSettlement;
import com.fundatechperu.shareway.payments.domain.model.SettlementStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de la tabla driver_settlements. */
public interface JpaDriverSettlementRepository extends JpaRepository<DriverSettlement, UUID> {

  Optional<DriverSettlement> findByTripId(UUID tripId);

  Optional<DriverSettlement> findByIdAndDriverId(UUID id, UUID driverId);

  List<DriverSettlement> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

  List<DriverSettlement> findByStatusOrderByCreatedAtDesc(SettlementStatus status);

  List<DriverSettlement> findAllByOrderByCreatedAtDesc();
}
