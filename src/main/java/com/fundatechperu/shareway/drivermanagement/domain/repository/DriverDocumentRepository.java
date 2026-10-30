package com.fundatechperu.shareway.drivermanagement.domain.repository;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverDocumentRepository {

  DriverDocument save(DriverDocument document);

  Optional<DriverDocument> findById(UUID id);

  Optional<DriverDocument> findByIdAndDriverId(UUID id, UUID driverId);

  List<DriverDocument> findAllByDriverId(UUID driverId);
}
