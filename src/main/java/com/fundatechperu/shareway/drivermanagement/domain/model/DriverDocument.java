package com.fundatechperu.shareway.drivermanagement.domain.model;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    schema = "dm",
    name = "driver_documents",
    indexes = @Index(name = "idx_driver_documents_driver_id", columnList = "driver_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriverDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "driver_id", nullable = false)
  private UUID driverId;

  @Enumerated(EnumType.STRING)
  @Column(name = "document_type", nullable = false, length = 30)
  private DocumentType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DocumentStatus status;

  @Column(name = "original_filename", nullable = false)
  private String originalFilename;

  @Column(name = "content_type", nullable = false, length = 50)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "storage_key", nullable = false, unique = true, length = 100)
  private String storageKey;

  @Column(name = "rejection_reason", length = 500)
  private String rejectionReason;

  @Column(name = "reviewed_by")
  private UUID reviewedBy;

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  private DriverDocument(
      UUID driverId,
      DocumentType type,
      String originalFilename,
      String contentType,
      long sizeBytes,
      String storageKey) {
    this.driverId = driverId;
    this.type = type;
    this.status = DocumentStatus.PENDING;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
    this.sizeBytes = sizeBytes;
    this.storageKey = storageKey;
    this.uploadedAt = Instant.now();
  }

  public static DriverDocument upload(
      UUID driverId,
      DocumentType type,
      String originalFilename,
      String contentType,
      long sizeBytes,
      String storageKey) {
    return new DriverDocument(driverId, type, originalFilename, contentType, sizeBytes, storageKey);
  }

  public void approve(UUID reviewerId) {
    ensurePending();
    this.status = DocumentStatus.APPROVED;
    this.rejectionReason = null;
    markReviewed(reviewerId);
  }

  public void reject(UUID reviewerId, String reason) {
    if (reason == null || reason.isBlank()) {
      throw new DomainValidationException("reason", "reason is required when rejecting a document");
    }
    ensurePending();
    this.status = DocumentStatus.REJECTED;
    this.rejectionReason = reason.trim();
    markReviewed(reviewerId);
  }

  public boolean isApproved() {
    return status == DocumentStatus.APPROVED;
  }

  private void ensurePending() {
    if (status != DocumentStatus.PENDING) {
      throw new ConflictException("Document has already been reviewed");
    }
  }

  private void markReviewed(UUID reviewerId) {
    this.reviewedBy = reviewerId;
    this.reviewedAt = Instant.now();
  }
}
