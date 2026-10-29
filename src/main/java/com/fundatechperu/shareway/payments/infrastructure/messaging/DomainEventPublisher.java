package com.fundatechperu.shareway.payments.infrastructure.messaging;

import com.fundatechperu.shareway.payments.domain.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publica los eventos de dominio sobre el bus interno de Spring mientras no exista un broker de
 * mensajes.
 */
@Component
public class DomainEventPublisher {

  private final ApplicationEventPublisher publisher;

  public DomainEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  public void publish(DomainEvent event) {
    publisher.publishEvent(event);
  }
}
