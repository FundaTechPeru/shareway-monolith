package com.fundatechperu.shareway.shared.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Authorization rules contributed by one bounded context to the single filter chain.
 *
 * <p>Each context declares the roles its paths require; removing a context from the monolith
 * removes its rules with it. Paths not covered by any rule only require authentication.
 */
@FunctionalInterface
public interface SecurityRules {

  void configure(
      AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
          authorize);
}
