package com.fundatechperu.shareway.drivermanagement.interfaces.rest.dto;

import com.fundatechperu.shareway.drivermanagement.application.AvailabilitySlotCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record WeeklyAvailabilityRequest(
    @NotNull @Size(max = 50) List<@NotNull @Valid AvailabilitySlotRequest> slots) {

  public List<AvailabilitySlotCommand> toCommands() {
    return slots.stream().map(AvailabilitySlotRequest::toCommand).toList();
  }
}
