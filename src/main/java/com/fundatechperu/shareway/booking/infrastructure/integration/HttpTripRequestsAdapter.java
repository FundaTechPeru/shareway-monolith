package com.fundatechperu.shareway.booking.infrastructure.integration;

import com.fundatechperu.shareway.booking.domain.service.TripRequestsPort;
import com.fundatechperu.shareway.shared.exception.ServiceUnavailableException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link TripRequestsPort} for the Trip Planning &amp; Matching service at {@code
 * app.services.trip-planning-matching.url}.
 *
 * <p>The service has no endpoint for a single trip request yet, so the adapter reads {@code GET
 * /api/v1/trip-requests} and looks for the id. The caller's token is passed along in case the
 * service starts checking it. The passenger is compared as a UUID, the contract of the UUID version
 * of the service.
 */
@Component
public class HttpTripRequestsAdapter implements TripRequestsPort {

  private final RestClient restClient;

  public HttpTripRequestsAdapter(
      @Value("${app.services.trip-planning-matching.url}") String baseUrl,
      @Value("${app.services.timeout-ms:3000}") long timeoutMs) {
    Duration timeout = Duration.ofMillis(timeoutMs);
    JdkClientHttpRequestFactory requestFactory =
        new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(timeout).build());
    requestFactory.setReadTimeout(timeout);
    this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
  }

  @Override
  public boolean belongsTo(UUID tripRequestId, UUID passengerId) {
    List<TripRequest> tripRequests;
    try {
      tripRequests =
          restClient
              .get()
              .uri("/api/v1/trip-requests")
              .headers(
                  headers -> {
                    String authorization = currentAuthorizationHeader();
                    if (authorization != null) {
                      headers.set(HttpHeaders.AUTHORIZATION, authorization);
                    }
                  })
              .retrieve()
              .body(new ParameterizedTypeReference<List<TripRequest>>() {});
    } catch (RestClientException ex) {
      throw new ServiceUnavailableException("Trip Planning & Matching is not available");
    }
    if (tripRequests == null) {
      return false;
    }
    return tripRequests.stream()
        .anyMatch(
            tripRequest ->
                tripRequestId.toString().equals(tripRequest.id())
                    && passengerId.toString().equals(tripRequest.passengerId()));
  }

  private static String currentAuthorizationHeader() {
    if (RequestContextHolder.getRequestAttributes()
        instanceof ServletRequestAttributes attributes) {
      return attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
    return null;
  }

  /** The fields of a trip request that the booking needs, read as text to compare them. */
  record TripRequest(String id, String passengerId) {}
}
