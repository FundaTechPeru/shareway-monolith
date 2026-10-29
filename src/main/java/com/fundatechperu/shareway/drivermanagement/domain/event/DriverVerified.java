package com.fundatechperu.shareway.drivermanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

public record DriverVerified(UUID driverId, String email, Instant occurredAt)
    implements DomainEvent {

  public DriverVerified(UUID driverId, String email) {
    this(driverId, email, Instant.now());
  }
}
