package com.fundatechperu.shareway.drivermanagement.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DriverVerificationRuleTest {

  private static final UUID ADMIN_ID = new UUID(0, 99);

  @Test
  void requiredDocumentTypesAreLicenseNationalIdAndCriminalRecord() {
    assertThat(DocumentType.requiredForVerification())
        .containsExactlyInAnyOrder(
            DocumentType.DRIVERS_LICENSE, DocumentType.NATIONAL_ID, DocumentType.CRIMINAL_RECORD);
  }

  @Test
  void driverIsVerifiedOnlyWhenAllThreeRequiredDocumentsAreApproved() {
    User driver = pendingDriver();
    DriverDocument license = approved(DocumentType.DRIVERS_LICENSE);
    DriverDocument nationalId = approved(DocumentType.NATIONAL_ID);
    DriverDocument criminalRecord = document(DocumentType.CRIMINAL_RECORD);

    assertThat(driver.verifyIfRequirementsMet(List.of(license, nationalId, criminalRecord)))
        .isFalse();
    assertThat(driver.getDriverStatus()).isEqualTo(DriverStatus.PENDING_VERIFICATION);

    criminalRecord.approve(ADMIN_ID);

    assertThat(driver.verifyIfRequirementsMet(List.of(license, nationalId, criminalRecord)))
        .isTrue();
    assertThat(driver.getDriverStatus()).isEqualTo(DriverStatus.VERIFIED);
  }

  @Test
  void rejectedRequiredDocumentPreventsVerification() {
    User driver = pendingDriver();
    DriverDocument criminalRecord = document(DocumentType.CRIMINAL_RECORD);
    criminalRecord.reject(ADMIN_ID, "Expired certificate");

    boolean verified =
        driver.verifyIfRequirementsMet(
            List.of(
                approved(DocumentType.DRIVERS_LICENSE),
                approved(DocumentType.NATIONAL_ID),
                criminalRecord));

    assertThat(verified).isFalse();
    assertThat(driver.getDriverStatus()).isEqualTo(DriverStatus.PENDING_VERIFICATION);
  }

  @Test
  void vehicleRegistrationDoesNotReplaceARequiredDocument() {
    User driver = pendingDriver();

    boolean verified =
        driver.verifyIfRequirementsMet(
            List.of(
                approved(DocumentType.DRIVERS_LICENSE),
                approved(DocumentType.NATIONAL_ID),
                approved(DocumentType.VEHICLE_REGISTRATION)));

    assertThat(verified).isFalse();
  }

  @Test
  void verificationHappensOnlyOnce() {
    User driver = pendingDriver();
    List<DriverDocument> documents =
        List.of(
            approved(DocumentType.DRIVERS_LICENSE),
            approved(DocumentType.NATIONAL_ID),
            approved(DocumentType.CRIMINAL_RECORD));

    assertThat(driver.verifyIfRequirementsMet(documents)).isTrue();
    assertThat(driver.verifyIfRequirementsMet(documents)).isFalse();
    assertThat(driver.getDriverStatus()).isEqualTo(DriverStatus.VERIFIED);
  }

  @Test
  void passengerIsNeverVerified() {
    User passenger =
        User.registerPassenger("ana.torres@upc.edu.pe", "hash", "Ana Torres", "+51987654321");

    boolean verified =
        passenger.verifyIfRequirementsMet(
            List.of(
                approved(DocumentType.DRIVERS_LICENSE),
                approved(DocumentType.NATIONAL_ID),
                approved(DocumentType.CRIMINAL_RECORD)));

    assertThat(verified).isFalse();
    assertThat(passenger.getDriverStatus()).isNull();
  }

  private static User pendingDriver() {
    User user =
        User.registerPassenger("carlos.diaz@upc.edu.pe", "hash", "Carlos Diaz", "+51987654321");
    user.registerAsDriver("Q12345678", new EmergencyContact("Rosa Diaz", "+51911222333", "Mother"));
    return user;
  }

  private static DriverDocument document(DocumentType type) {
    return DriverDocument.upload(null, type, "file.pdf", "application/pdf", 1024, type + ".pdf");
  }

  private static DriverDocument approved(DocumentType type) {
    DriverDocument document = document(type);
    document.approve(ADMIN_ID);
    return document;
  }
}
