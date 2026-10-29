package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.domain.model.DocumentStatus;
import com.fundatechperu.shareway.drivermanagement.domain.model.DocumentType;
import com.fundatechperu.shareway.drivermanagement.domain.model.DriverDocument;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    UUID driverId,
    DocumentType type,
    DocumentStatus status,
    String originalFilename,
    String contentType,
    long sizeBytes,
    String rejectionReason,
    Instant uploadedAt,
    Instant reviewedAt) {

  public static DocumentResponse from(DriverDocument document) {
    return new DocumentResponse(
        document.getId(),
        document.getDriverId(),
        document.getType(),
        document.getStatus(),
        document.getOriginalFilename(),
        document.getContentType(),
        document.getSizeBytes(),
        document.getRejectionReason(),
        document.getUploadedAt(),
        document.getReviewedAt());
  }
}
