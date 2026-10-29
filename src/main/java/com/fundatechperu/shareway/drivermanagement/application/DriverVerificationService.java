package com.fundatechperu.shareway.drivermanagement.application;

import com.fundatechperu.shareway.drivermanagement.domain.event.DocumentRejected;
import com.fundatechperu.shareway.drivermanagement.domain.event.DomainEventPublisher;
import com.fundatechperu.shareway.drivermanagement.domain.event.DriverVerified;
import com.fundatechperu.shareway.drivermanagement.domain.model.DriverDocument;
import com.fundatechperu.shareway.drivermanagement.domain.model.User;
import com.fundatechperu.shareway.drivermanagement.domain.repository.DriverDocumentRepository;
import com.fundatechperu.shareway.drivermanagement.domain.repository.UserRepository;
import com.fundatechperu.shareway.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverVerificationService {

  private final DriverDocumentRepository documentRepository;
  private final UserRepository userRepository;
  private final DomainEventPublisher eventPublisher;

  public DriverVerificationService(
      DriverDocumentRepository documentRepository,
      UserRepository userRepository,
      DomainEventPublisher eventPublisher) {
    this.documentRepository = documentRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public DriverDocument review(
      UUID documentId, UUID reviewerId, ReviewDecision decision, String reason) {
    DriverDocument document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

    switch (decision) {
      case APPROVED -> document.approve(reviewerId);
      case REJECTED -> document.reject(reviewerId, reason);
    }
    DriverDocument reviewed = documentRepository.save(document);

    if (decision == ReviewDecision.REJECTED) {
      eventPublisher.publish(
          new DocumentRejected(
              reviewed.getId(),
              reviewed.getDriverId(),
              reviewed.getType(),
              reviewed.getRejectionReason()));
    } else {
      verifyDriverIfEligible(reviewed.getDriverId());
    }
    return reviewed;
  }

  private void verifyDriverIfEligible(UUID driverId) {
    User driver =
        userRepository
            .findById(driverId)
            .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
    if (driver.verifyIfRequirementsMet(documentRepository.findAllByDriverId(driverId))) {
      userRepository.save(driver);
      eventPublisher.publish(new DriverVerified(driver.getId(), driver.getEmail()));
    }
  }
}
