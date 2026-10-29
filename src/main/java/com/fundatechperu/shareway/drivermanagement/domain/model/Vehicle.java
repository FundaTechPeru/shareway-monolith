package com.fundatechperu.shareway.drivermanagement.domain.model;

import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    schema = "dm",
    name = "vehicles",
    indexes = @Index(name = "idx_vehicles_driver_id", columnList = "driver_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

  private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z0-9]{3}-?[A-Z0-9]{3}$");

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "driver_id", nullable = false)
  private UUID driverId;

  @Column(nullable = false, unique = true, length = 7)
  private String plate;

  @Column(nullable = false, length = 50)
  private String brand;

  @Column(nullable = false, length = 50)
  private String model;

  @Column(name = "manufacture_year", nullable = false)
  private int year;

  @Column(nullable = false, length = 30)
  private String color;

  @Column(name = "seat_count", nullable = false)
  private int seats;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private Vehicle(UUID driverId) {
    this.driverId = driverId;
    this.createdAt = Instant.now();
  }

  public static Vehicle register(
      UUID driverId, String plate, String brand, String model, int year, String color, int seats) {
    Vehicle vehicle = new Vehicle(driverId);
    vehicle.update(plate, brand, model, year, color, seats);
    return vehicle;
  }

  public void update(String plate, String brand, String model, int year, String color, int seats) {
    this.plate = normalizePlate(plate);
    this.brand = brand.trim();
    this.model = model.trim();
    this.year = year;
    this.color = color.trim();
    this.seats = seats;
    this.updatedAt = Instant.now();
  }

  /**
   * Normalizes a plate such as {@code abc123} or {@code abc-123} to its canonical form {@code
   * ABC-123}.
   */
  public static String normalizePlate(String plate) {
    String candidate = plate == null ? "" : plate.trim().toUpperCase(Locale.ROOT);
    if (!PLATE_PATTERN.matcher(candidate).matches()) {
      throw new DomainValidationException(
          "plate", "plate must have 6 letters or digits with an optional hyphen");
    }
    String compact = candidate.replace("-", "");
    return compact.substring(0, 3) + "-" + compact.substring(3);
  }
}
