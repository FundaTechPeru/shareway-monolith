package com.fundatechperu.shareway.drivermanagement.domain.event;

import com.fundatechperu.shareway.drivermanagement.domain.model.DocumentType;
import java.time.Instant;
import java.util.UUID;

public record DocumentRejected(
    UUID documentId, UUID driverId, DocumentType documentType, String reason, Instant occurredAt)
    implements DomainEvent {

  public DocumentRejected(
      UUID documentId, UUID driverId, DocumentType documentType, String reason) {
    this(documentId, driverId, documentType, reason, Instant.now());
  }
}
