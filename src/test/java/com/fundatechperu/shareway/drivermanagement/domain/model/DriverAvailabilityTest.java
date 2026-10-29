package com.fundatechperu.shareway.drivermanagement.domain.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.shared.exception.DomainValidationException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DriverAvailabilityTest {

  @Test
  void startMustBeBeforeEnd() {
    assertThatThrownBy(() -> slot(DayOfWeek.MONDAY, "10:00", "09:00"))
        .isInstanceOf(DomainValidationException.class);
    assertThatThrownBy(() -> slot(DayOfWeek.MONDAY, "10:00", "10:00"))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void overlappingSlotsOnTheSameDayAreRejected() {
    List<DriverAvailability> schedule =
        List.of(
            slot(DayOfWeek.MONDAY, "07:00", "09:00"),
            slot(DayOfWeek.TUESDAY, "07:00", "09:00"),
            slot(DayOfWeek.MONDAY, "08:59", "10:00"));

    assertThatThrownBy(() -> DriverAvailability.ensureNoOverlaps(schedule))
        .isInstanceOf(DomainValidationException.class)
        .hasMessageContaining("MONDAY");
  }

  @Test
  void slotContainedInAnotherIsAnOverlap() {
    List<DriverAvailability> schedule =
        List.of(slot(DayOfWeek.FRIDAY, "06:00", "12:00"), slot(DayOfWeek.FRIDAY, "08:00", "09:00"));

    assertThatThrownBy(() -> DriverAvailability.ensureNoOverlaps(schedule))
        .isInstanceOf(DomainValidationException.class);
  }

  @Test
  void adjacentSlotsAndOtherDaysAreAllowed() {
    List<DriverAvailability> schedule =
        List.of(
            slot(DayOfWeek.MONDAY, "07:00", "09:00"),
            slot(DayOfWeek.MONDAY, "09:00", "11:00"),
            slot(DayOfWeek.TUESDAY, "07:00", "09:00"));

    assertThatCode(() -> DriverAvailability.ensureNoOverlaps(schedule)).doesNotThrowAnyException();
  }

  private static DriverAvailability slot(DayOfWeek dayOfWeek, String startTime, String endTime) {
    return DriverAvailability.create(
        new UUID(0, 1), dayOfWeek, LocalTime.parse(startTime), LocalTime.parse(endTime));
  }
}
