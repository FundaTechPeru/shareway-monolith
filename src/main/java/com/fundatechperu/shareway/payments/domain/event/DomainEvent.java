package com.fundatechperu.shareway.payments.domain.event;

import java.time.Instant;

/** Hecho relevante del dominio de pagos que se publica hacia otros componentes. */
public interface DomainEvent {

  Instant occurredAt();
}
