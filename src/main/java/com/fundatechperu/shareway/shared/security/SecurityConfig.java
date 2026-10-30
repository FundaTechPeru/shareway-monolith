package com.fundatechperu.shareway.shared.security;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * The single security filter chain of the monolith: stateless, JWT based and closed by default.
 *
 * <p>Every bounded context contributes its role rules through {@link SecurityRules} beans; any
 * other path only requires a valid token. Service-to-service endpoints under {@code
 * /api/v1/internal/**} take the internal API key instead of a user token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  static final String[] PUBLIC_ENDPOINTS = {
    "/actuator/health",
    "/actuator/health/**",
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/error"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      SecurityErrorHandler errorHandler,
      JwtService jwtService,
      ObjectProvider<AuthenticatedUserResolver> userResolver,
      ObjectProvider<SecurityRules> contextRules,
      @Value("${app.internal.api-key}") String internalApiKey) {
    AuthenticatedUserResolver resolver = userResolver.getIfAvailable(() -> user -> user);
    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(errorHandler).accessDeniedHandler(errorHandler))
        .authorizeHttpRequests(
            authorize -> {
              authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
              authorize
                  .requestMatchers(InternalApiKeyFilter.INTERNAL_PATHS)
                  .hasRole(InternalApiKeyFilter.INTERNAL_ROLE);
              contextRules.orderedStream().forEach(rules -> rules.configure(authorize));
              authorize.anyRequest().authenticated();
            })
        .addFilterBefore(
            new InternalApiKeyFilter(internalApiKey, errorHandler),
            UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtService, resolver),
            UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        List.of(
            "Authorization",
            "Content-Type",
            "Idempotency-Key",
            "X-Request-Id",
            InternalApiKeyFilter.HEADER));
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
