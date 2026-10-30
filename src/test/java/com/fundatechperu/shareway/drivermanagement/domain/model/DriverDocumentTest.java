package com.fundatechperu.shareway.drivermanagement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DriverDocumentTest {

  private static final UUID ADMIN_ID = new UUID(0, 99);

  @Test
  void uploadedDocumentStartsPending() {
    DriverDocument document = license();

    assertThat(document.getStatus()).isEqualTo(DocumentStatus.PENDING);
    assertThat(document.getUploadedAt()).isNotNull();
    assertThat(document.getReviewedAt()).isNull();
  }

  @Test
  void approvalRecordsTheReviewer() {
    DriverDocument document = license();

    document.approve(ADMIN_ID);

    assertThat(document.isApproved()).isTrue();
    assertThat(document.getReviewedBy()).isEqualTo(ADMIN_ID);
    assertThat(document.getReviewedAt()).isNotNull();
  }

  @Test
  void rejectionStoresTheTrimmedReason() {
    DriverDocument document = license();

    document.reject(ADMIN_ID, "  The document is illegible ");

    assertThat(document.getStatus()).isEqualTo(DocumentStatus.REJECTED);
    assertThat(document.getRejectionReason()).isEqualTo("The document is illegible");
  }

  @Test
  void rejectionRequiresAReason() {
    DriverDocument document = license();

    assertThatThrownBy(() -> document.reject(ADMIN_ID, "   "))
        .isInstanceOf(DomainValidationException.class)
        .satisfies(
            ex -> assertThat(((DomainValidationException) ex).getField()).contains("reason"));
    assertThat(document.getStatus()).isEqualTo(DocumentStatus.PENDING);
  }

  @Test
  void documentCannotBeReviewedTwice() {
    DriverDocument document = license();
    document.approve(ADMIN_ID);

    assertThatThrownBy(() -> document.reject(ADMIN_ID, "Changed my mind"))
        .isInstanceOf(ConflictException.class);
    assertThat(document.isApproved()).isTrue();
  }

  private static DriverDocument license() {
    return DriverDocument.upload(
        new UUID(0, 1),
        DocumentType.DRIVERS_LICENSE,
        "license.pdf",
        "application/pdf",
        1024,
        "key.pdf");
  }
}
