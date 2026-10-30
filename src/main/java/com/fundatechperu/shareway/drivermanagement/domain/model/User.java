package com.fundatechperu.shareway.drivermanagement.domain.model;

import com.fundatechperu.shareway.shared.exception.ConflictException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "dm", name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 120)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(nullable = false, length = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(name = "license_number", unique = true, length = 12)
  private String licenseNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "driver_status", length = 30)
  private DriverStatus driverStatus;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "emergency_contact_id", unique = true)
  private EmergencyContact emergencyContact;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  private User(String email, String passwordHash, String fullName, String phone, Role role) {
    this.email = normalizeEmail(email);
    this.passwordHash = passwordHash;
    this.fullName = fullName.trim();
    this.phone = phone.trim();
    this.role = role;
    this.createdAt = Instant.now();
  }

  public static User registerPassenger(
      String email, String passwordHash, String fullName, String phone) {
    return new User(email, passwordHash, fullName, phone, Role.PASSENGER);
  }

  public static User createAdmin(String email, String passwordHash, String fullName, String phone) {
    return new User(email, passwordHash, fullName, phone, Role.ADMIN);
  }

  public static String normalizeEmail(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }

  public boolean isDriver() {
    return role == Role.DRIVER;
  }

  public void registerAsDriver(String licenseNumber, EmergencyContact emergencyContact) {
    if (isDriver()) {
      throw new ConflictException("User is already registered as a driver");
    }
    if (role != Role.PASSENGER) {
      throw new ConflictException("Only passengers can register as drivers");
    }
    this.licenseNumber = licenseNumber;
    this.emergencyContact = emergencyContact;
    this.role = Role.DRIVER;
    this.driverStatus = DriverStatus.PENDING_VERIFICATION;
  }

  /**
   * Marks the driver as VERIFIED when every document type required for verification has an approved
   * document.
   *
   * @return {@code true} only if this call changed the status to VERIFIED
   */
  public boolean verifyIfRequirementsMet(Collection<DriverDocument> documents) {
    if (!isDriver() || driverStatus == DriverStatus.VERIFIED) {
      return false;
    }
    Set<DocumentType> approvedTypes =
        documents.stream()
            .filter(document -> Objects.equals(document.getDriverId(), id))
            .filter(DriverDocument::isApproved)
            .map(DriverDocument::getType)
            .collect(Collectors.toSet());
    if (!approvedTypes.containsAll(DocumentType.requiredForVerification())) {
      return false;
    }
    this.driverStatus = DriverStatus.VERIFIED;
    return true;
  }
}
