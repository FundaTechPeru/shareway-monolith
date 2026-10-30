package com.fundatechperu.shareway.shared.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global OpenAPI definition shared by every group: API information and the bearer JWT scheme.
 *
 * <p>The scheme is required by default, so the Swagger UI "Authorize" button sends the token on
 * every call; public operations opt out with an empty {@code @SecurityRequirements}.
 */
@Configuration
public class OpenApiConfig {

  public static final String BEARER_SCHEME = "bearerAuth";
  public static final String INTERNAL_API_KEY_SCHEME = "internalApiKey";

  @Bean
  public OpenAPI sharewayOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("ShareWay Monolith")
                .version("0.1.0")
                .description(
                    "Scheduled shared rides for university students with verified drivers. "
                        + "One document per bounded context: Trip Planning & Matching, Booking & "
                        + "Pricing and Payments & Settlement. Log in with POST /api/v1/auth/login "
                        + "of Driver Management (through the API gateway) and use the accessToken "
                        + "with Authorize."))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "Access token returned by POST /api/v1/auth/login of Driver Management"))
                .addSecuritySchemes(
                    INTERNAL_API_KEY_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Internal-Api-Key")
                        .description("Service-to-service key of the /api/v1/internal endpoints")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }
}
