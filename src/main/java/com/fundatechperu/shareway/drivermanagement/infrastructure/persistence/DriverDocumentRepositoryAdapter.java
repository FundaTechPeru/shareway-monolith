package com.fundatechperu.shareway.drivermanagement.infrastructure.persistence;

import com.fundatechperu.shareway.drivermanagement.domain.model.DriverDocument;
import com.fundatechperu.shareway.drivermanagement.domain.repository.DriverDocumentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class DriverDocumentRepositoryAdapter implements DriverDocumentRepository {

  private final SpringDataDriverDocumentRepository jpaRepository;

  public DriverDocumentRepositoryAdapter(SpringDataDriverDocumentRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public DriverDocument save(DriverDocument document) {
    return jpaRepository.save(document);
  }

  @Override
  public Optional<DriverDocument> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<DriverDocument> findByIdAndDriverId(UUID id, UUID driverId) {
    return jpaRepository.findByIdAndDriverId(id, driverId);
  }

  @Override
  public List<DriverDocument> findAllByDriverId(UUID driverId) {
    return jpaRepository.findAllByDriverIdOrderByUploadedAtAsc(driverId);
  }
}
