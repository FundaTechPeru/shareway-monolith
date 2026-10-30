package com.fundatechperu.shareway.shared.openapi;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * One OpenAPI document per bounded context, served at {@code /v3/api-docs/<group>}.
 *
 * <p>The path prefixes match the route table of the API gateway, so when a context is extracted the
 * gateway can swap this group for the {@code /v3/api-docs} of the new service.
 */
@Configuration
public class OpenApiGroupsConfig {

  public static final String BOOKING = "booking";

  @Bean
  public GroupedOpenApi bookingApi() {
    return GroupedOpenApi.builder()
        .group(BOOKING)
        .displayName("Booking & Pricing")
        .pathsToMatch("/api/v1/bookings/**", "/api/v1/fares/**", "/api/v1/fare-rules/**")
        .build();
  }
}
