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

  public static final String TRIP_PLANNING = "tripplanning";
  public static final String BOOKING = "booking";
  public static final String PAYMENTS = "payments";

  @Bean
  public GroupedOpenApi tripPlanningApi() {
    return GroupedOpenApi.builder()
        .group(TRIP_PLANNING)
        .displayName("Trip Planning & Matching")
        .pathsToMatch(
            "/api/v1/trip-requests/**", "/api/v1/driver-availabilities/**", "/api/v1/info")
        .build();
  }

  @Bean
  public GroupedOpenApi bookingApi() {
    return GroupedOpenApi.builder()
        .group(BOOKING)
        .displayName("Booking & Pricing")
        .pathsToMatch("/api/v1/bookings/**", "/api/v1/fares/**", "/api/v1/fare-rules/**")
        .build();
  }

  @Bean
  public GroupedOpenApi paymentsApi() {
    return GroupedOpenApi.builder()
        .group(PAYMENTS)
        .displayName("Payments & Settlement")
        .pathsToMatch(
            "/api/v1/payments/**",
            "/api/v1/payment-methods/**",
            "/api/v1/settlements/**",
            "/api/v1/admin/settlements/**",
            "/api/v1/internal/payments/**",
            "/api/v1/internal/events/**")
        .build();
  }
}
