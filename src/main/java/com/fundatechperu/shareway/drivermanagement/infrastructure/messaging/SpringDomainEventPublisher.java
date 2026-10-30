package com.fundatechperu.shareway.drivermanagement.infrastructure.messaging;

import com.fundatechperu.shareway.drivermanagement.domain.event.DomainEvent;
import com.fundatechperu.shareway.drivermanagement.domain.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void publish(DomainEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
