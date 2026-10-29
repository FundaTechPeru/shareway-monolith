package com.fundatechperu.shareway.drivermanagement.domain.model;

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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    schema = "dm",
    name = "driver_availabilities",
    indexes = @Index(name = "idx_driver_availabilities_driver_id", columnList = "driver_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriverAvailability {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "driver_id", nullable = false)
  private UUID driverId;

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false, length = 10)
  private DayOfWeek dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  private DriverAvailability(
      UUID driverId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    this.driverId = driverId;
    this.dayOfWeek = dayOfWeek;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public static DriverAvailability create(
      UUID driverId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    if (!startTime.isBefore(endTime)) {
      throw new DomainValidationException(
          "slots",
          "startTime must be before endTime (%s %s-%s)".formatted(dayOfWeek, startTime, endTime));
    }
    return new DriverAvailability(driverId, dayOfWeek, startTime, endTime);
  }

  public boolean overlaps(DriverAvailability other) {
    return dayOfWeek == other.dayOfWeek
        && startTime.isBefore(other.endTime)
        && other.startTime.isBefore(endTime);
  }

  public static void ensureNoOverlaps(List<DriverAvailability> slots) {
    for (int i = 0; i < slots.size(); i++) {
      for (int j = i + 1; j < slots.size(); j++) {
        DriverAvailability first = slots.get(i);
        DriverAvailability second = slots.get(j);
        if (first.overlaps(second)) {
          throw new DomainValidationException(
              "slots",
              "Slots overlap on %s: %s-%s and %s-%s"
                  .formatted(
                      first.dayOfWeek,
                      first.startTime,
                      first.endTime,
                      second.startTime,
                      second.endTime));
        }
      }
    }
  }
}
