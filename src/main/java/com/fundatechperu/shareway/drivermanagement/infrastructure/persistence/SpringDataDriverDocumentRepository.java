package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDriverDocumentRepository extends JpaRepository<DriverDocument, UUID> {

  Optional<DriverDocument> findByIdAndDriverId(UUID id, UUID driverId);

  List<DriverDocument> findAllByDriverIdOrderByUploadedAtAsc(UUID driverId);
}
