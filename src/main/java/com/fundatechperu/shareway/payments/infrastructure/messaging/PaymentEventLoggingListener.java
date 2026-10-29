package com.fundatechperu.shareway.payments.infrastructure.messaging;

import com.fundatechperu.shareway.payments.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Registra en el log cada evento de dominio una vez confirmada la transacción que lo produjo. */
@Component
public class PaymentEventLoggingListener {

  private static final Logger log = LoggerFactory.getLogger(PaymentEventLoggingListener.class);

  @TransactionalEventListener(fallbackExecution = true)
  public void on(DomainEvent event) {
    log.info("Domain event {}: {}", event.getClass().getSimpleName(), event);
  }
}
