package com.fundatechperu.shareway.booking.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateBookingRequest {

  @NotNull private UUID requestId;
  @NotNull private UUID groupId;

  public UUID getRequestId() {
    return requestId;
  }

  public void setRequestId(UUID requestId) {
    this.requestId = requestId;
  }

  public UUID getGroupId() {
    return groupId;
  }

  public void setGroupId(UUID groupId) {
    this.groupId = groupId;
  }
}
