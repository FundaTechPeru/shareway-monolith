package com.fundatechperu.shareway.payments.infrastructure.config;

import com.fundatechperu.shareway.payments.domain.policy.CommissionPolicy;
import com.fundatechperu.shareway.payments.domain.policy.DefaultPenaltyPolicy;
import com.fundatechperu.shareway.payments.domain.policy.PenaltyPolicy;
import com.fundatechperu.shareway.payments.domain.policy.PercentageCommissionPolicy;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Construye las políticas de dominio con los parámetros definidos en app.payments de
 * application.yaml.
 */
@Configuration
public class DomainPolicyConfig {

  @Bean
  public CommissionPolicy commissionPolicy(
      @Value("${app.payments.commission-rate}") BigDecimal rate) {
    return new PercentageCommissionPolicy(rate);
  }

  @Bean
  public PenaltyPolicy penaltyPolicy(
      @Value("${app.payments.penalty-rate}") BigDecimal rate,
      @Value("${app.payments.penalty-window-minutes}") long windowMinutes) {
    return new DefaultPenaltyPolicy(rate, windowMinutes);
  }
}
