package com.fundatechperu.shareway.tripplanning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Seats a driver offers in an operation zone on a given date and time window, used to match trip
 * requests.
 *
 * <p>Driver Management keeps a weekly schedule with the same class name (US-19); this one is dated
 * and has its own entity name and table (tpm.driver_availabilities). See "Known issues" in the
 * README.
 */
@Entity(name = "TripPlanningDriverAvailability")
@Table(schema = "tpm", name = "driver_availabilities")
public class DriverAvailability {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @Column(nullable = false)
  private UUID driverId;

  @NotBlank
  @Column(nullable = false, length = 120)
  private String operationZone;

  @NotNull
  @Column(nullable = false)
  private LocalDate availableDate;

  @NotNull
  @Column(nullable = false)
  private LocalTime startTime;

  @NotNull
  @Column(nullable = false)
  private LocalTime endTime;

  @Min(1)
  @Column(nullable = false)
  private int availableSeats;

  protected DriverAvailability() {}

  public DriverAvailability(
      UUID driverId,
      String operationZone,
      LocalDate availableDate,
      LocalTime startTime,
      LocalTime endTime,
      int availableSeats) {
    this.driverId = driverId;
    this.operationZone = operationZone;
    this.availableDate = availableDate;
    this.startTime = startTime;
    this.endTime = endTime;
    this.availableSeats = availableSeats;
  }

  public UUID getId() {
    return id;
  }

  public UUID getDriverId() {
    return driverId;
  }

  public String getOperationZone() {
    return operationZone;
  }

  public LocalDate getAvailableDate() {
    return availableDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public int getAvailableSeats() {
    return availableSeats;
  }
}
