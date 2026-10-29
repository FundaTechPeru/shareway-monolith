package com.fundatechperu.shareway.drivermanagement.domain.event;

import java.time.Instant;

public interface DomainEvent {

  Instant occurredAt();
}
