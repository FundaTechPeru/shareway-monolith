package com.fundatechperu.shareway.tripplanning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(schema = "tpm", name = "trip_requests")
public class TripRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @Column(nullable = false)
  private UUID passengerId;

  @NotBlank
  @Column(nullable = false, length = 160)
  private String originLabel;

  @NotBlank
  @Column(nullable = false, length = 160)
  private String destinationLabel;

  @NotBlank
  @Column(nullable = false, length = 120)
  private String operationZone;

  @NotNull
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal originLat;

  @NotNull
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal originLng;

  @NotNull
  @DecimalMin("-90.0")
  @DecimalMax("90.0")
  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal destinationLat;

  @NotNull
  @DecimalMin("-180.0")
  @DecimalMax("180.0")
  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal destinationLng;

  @NotNull
  @Column(nullable = false)
  private LocalDate travelDate;

  @NotNull
  @Column(nullable = false)
  private LocalTime earliestDeparture;

  @NotNull
  @Column(nullable = false)
  private LocalTime latestDeparture;

  @Min(1)
  @Column(nullable = false)
  private int seatsRequested;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TripRequestStatus status = TripRequestStatus.PENDING_MATCH;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected TripRequest() {}

  public TripRequest(
      UUID passengerId,
      String originLabel,
      String destinationLabel,
      String operationZone,
      BigDecimal originLat,
      BigDecimal originLng,
      BigDecimal destinationLat,
      BigDecimal destinationLng,
      LocalDate travelDate,
      LocalTime earliestDeparture,
      LocalTime latestDeparture,
      int seatsRequested) {
    this.passengerId = passengerId;
    this.originLabel = originLabel;
    this.destinationLabel = destinationLabel;
    this.operationZone = operationZone;
    this.originLat = originLat;
    this.originLng = originLng;
    this.destinationLat = destinationLat;
    this.destinationLng = destinationLng;
    this.travelDate = travelDate;
    this.earliestDeparture = earliestDeparture;
    this.latestDeparture = latestDeparture;
    this.seatsRequested = seatsRequested;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPassengerId() {
    return passengerId;
  }

  public String getOriginLabel() {
    return originLabel;
  }

  public String getDestinationLabel() {
    return destinationLabel;
  }

  public String getOperationZone() {
    return operationZone;
  }

  public LocalDate getTravelDate() {
    return travelDate;
  }

  public LocalTime getEarliestDeparture() {
    return earliestDeparture;
  }

  public LocalTime getLatestDeparture() {
    return latestDeparture;
  }

  public int getSeatsRequested() {
    return seatsRequested;
  }

  public TripRequestStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
