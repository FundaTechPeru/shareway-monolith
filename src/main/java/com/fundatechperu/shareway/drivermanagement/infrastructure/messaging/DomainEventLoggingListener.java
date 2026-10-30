package com.fundatechperu.shareway.drivermanagement.infrastructure.messaging;

import com.fundatechperu.shareway.drivermanagement.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class DomainEventLoggingListener {

  private static final Logger log = LoggerFactory.getLogger(DomainEventLoggingListener.class);

  @TransactionalEventListener(fallbackExecution = true)
  public void onDomainEvent(DomainEvent event) {
    log.info("Domain event {}: {}", event.getClass().getSimpleName(), event);
  }
}
