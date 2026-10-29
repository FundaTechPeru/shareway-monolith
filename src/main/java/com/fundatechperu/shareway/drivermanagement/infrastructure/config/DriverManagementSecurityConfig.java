package com.fundatechperu.shareway.drivermanagement.infrastructure.config;

import com.fundatechperu.shareway.drivermanagement.domain.model.Role;
import com.fundatechperu.shareway.shared.security.SecurityRules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password hashing, the email/password check used by the login use case and the roles required by
 * the Driver Management paths.
 */
@Configuration
public class DriverManagementSecurityConfig {

  @Bean
  public SecurityRules driverManagementSecurityRules() {
    return authorize ->
        authorize
            .requestMatchers("/api/v1/driver-documents/**")
            .hasRole(Role.ADMIN.name())
            .requestMatchers("/api/v1/drivers/me/**")
            .hasRole(Role.DRIVER.name());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }
}
