package com.fundatechperu.shareway.drivermanagement.domain.event;

public interface DomainEventPublisher {

  void publish(DomainEvent event);
}
