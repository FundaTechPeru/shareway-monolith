package com.fundatechperu.shareway.payments.infrastructure.config;

import com.fundatechperu.shareway.shared.security.SecurityRules;
import com.fundatechperu.shareway.shared.security.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Roles required by the Payments & Settlement paths of the shared filter chain. */
@Configuration
public class PaymentsSecurityConfig {

  @Bean
  public SecurityRules paymentsSecurityRules() {
    return authorize ->
        authorize
            .requestMatchers("/api/v1/payment-methods/**", "/api/v1/payments/**")
            .hasRole(UserRole.PASSENGER.name())
            .requestMatchers("/api/v1/settlements/**")
            .hasRole(UserRole.DRIVER.name())
            .requestMatchers("/api/v1/admin/settlements/**")
            .hasRole(UserRole.ADMIN.name());
  }
}
