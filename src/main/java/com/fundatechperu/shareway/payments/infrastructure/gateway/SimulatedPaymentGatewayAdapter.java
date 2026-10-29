package com.fundatechperu.shareway.payments.infrastructure.gateway;

import com.fundatechperu.shareway.payments.domain.exception.GatewayUnavailableException;
import com.fundatechperu.shareway.payments.domain.model.Money;
import com.fundatechperu.shareway.payments.domain.repository.PaymentGateway;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pasarela simulada y determinista, sin llamadas externas: {@code tok_declined} se rechaza, {@code
 * tok_unavailable} simula una caída y cualquier otro token se aprueba.
 */
@Component
public class SimulatedPaymentGatewayAdapter implements PaymentGateway {

  private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGatewayAdapter.class);

  static final String DECLINED_TOKEN = "tok_declined";
  static final String UNAVAILABLE_TOKEN = "tok_unavailable";

  @Override
  public GatewayAuthorization authorize(String cardToken, Money amount) {
    if (UNAVAILABLE_TOKEN.equals(cardToken)) {
      throw new GatewayUnavailableException("Payment gateway is unavailable");
    }
    if (DECLINED_TOKEN.equals(cardToken)) {
      return new GatewayAuthorization(false, null);
    }
    return new GatewayAuthorization(true, "sim_" + UUID.randomUUID());
  }

  @Override
  public void capture(String providerRef, Money amount) {
    log.info("Simulated capture of {} on {}", amount, providerRef);
  }

  @Override
  public void voidAuthorization(String providerRef) {
    log.info("Simulated void of {}", providerRef);
  }
}
