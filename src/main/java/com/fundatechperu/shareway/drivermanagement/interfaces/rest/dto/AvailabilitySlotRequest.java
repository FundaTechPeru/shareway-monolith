package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fundatechperu.shareway.drivermanagement.application.AvailabilitySlotCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilitySlotRequest(
    @NotNull DayOfWeek dayOfWeek,
    @NotNull @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "07:00")
        LocalTime startTime,
    @NotNull @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "09:00")
        LocalTime endTime) {

  public AvailabilitySlotCommand toCommand() {
    return new AvailabilitySlotCommand(dayOfWeek, startTime, endTime);
  }
}
