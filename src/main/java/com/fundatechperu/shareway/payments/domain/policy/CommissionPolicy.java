package com.fundatechperu.shareway.payments.domain.policy;

import com.fundatechperu.shareway.payments.domain.model.Money;

/** Estrategia para calcular la comisión de la plataforma sobre el monto bruto de un viaje. */
public interface CommissionPolicy {

  Money commissionFor(Money gross);
}
