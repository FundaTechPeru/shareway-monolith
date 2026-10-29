package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fundatechperu.shareway.drivermanagement.domain.model.DriverAvailability;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record WeeklyAvailabilityResponse(List<Slot> slots) {

  public record Slot(
      DayOfWeek dayOfWeek,
      @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "07:00")
          LocalTime startTime,
      @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "09:00")
          LocalTime endTime) {}

  public static WeeklyAvailabilityResponse from(List<DriverAvailability> schedule) {
    return new WeeklyAvailabilityResponse(
        schedule.stream()
            .map(slot -> new Slot(slot.getDayOfWeek(), slot.getStartTime(), slot.getEndTime()))
            .toList());
  }
}
