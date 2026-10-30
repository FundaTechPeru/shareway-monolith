package com.fundatechperu.shareway.shared.security;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link AuthenticatedUserResolver} backed by the extracted Driver Management service: it asks
 * {@code GET /api/v1/users/me} with the caller's own token, so a role change (for example PASSENGER
 * to DRIVER) applies without logging in again.
 *
 * <p>Active when {@code app.services.driver-management.url} is set. When the service does not
 * answer, answers with an error or describes another user, the claims of the token are used as they
 * are.
 */
@Component
@ConditionalOnExpression("!'${app.services.driver-management.url:}'.isBlank()")
public class DriverManagementUserResolver implements AuthenticatedUserResolver {

  private static final Logger log = LoggerFactory.getLogger(DriverManagementUserResolver.class);

  private final RestClient restClient;

  public DriverManagementUserResolver(
      @Value("${app.services.driver-management.url}") String baseUrl,
      @Value("${app.services.timeout-ms:3000}") long timeoutMs) {
    Duration timeout = Duration.ofMillis(timeoutMs);
    JdkClientHttpRequestFactory requestFactory =
        new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(timeout).build());
    requestFactory.setReadTimeout(timeout);
    this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
  }

  @Override
  public AuthenticatedUser resolve(AuthenticatedUser fromToken) {
    String authorization = currentAuthorizationHeader();
    if (authorization == null) {
      return fromToken;
    }
    try {
      UserProfile profile =
          restClient
              .get()
              .uri("/api/v1/users/me")
              .header(HttpHeaders.AUTHORIZATION, authorization)
              .retrieve()
              .body(UserProfile.class);
      if (profile == null || !fromToken.id().equals(profile.id())) {
        return fromToken;
      }
      return new AuthenticatedUser(profile.id(), profile.email(), UserRole.valueOf(profile.role()));
    } catch (RestClientException | IllegalArgumentException | NullPointerException ex) {
      log.debug("Driver Management did not refresh user {}: {}", fromToken.id(), ex.getMessage());
      return fromToken;
    }
  }

  private static String currentAuthorizationHeader() {
    if (RequestContextHolder.getRequestAttributes()
        instanceof ServletRequestAttributes attributes) {
      return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
    return null;
  }

  /** The fields of the Driver Management user profile that the monolith needs. */
  record UserProfile(UUID id, String email, String role) {}
}
