package com.fundatechperu.shareway.booking.infrastructure.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fundatechperu.shareway.shared.exception.ServiceUnavailableException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Runs the adapter against a stub of {@code GET /api/v1/trip-requests} that follows the contract of
 * the UUID version of Trip Planning &amp; Matching.
 */
class HttpTripRequestsAdapterTest {

  private static final UUID REQUEST_ID = UUID.fromString("5e6f7a8b-9c0d-4e1f-a2b3-c4d5e6f7a8b9");
  private static final UUID PASSENGER_ID = UUID.fromString("7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f");

  private final AtomicReference<String> receivedAuthorization = new AtomicReference<>();
  private HttpServer server;
  private int status = 200;

  @BeforeEach
  void startStub() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/api/v1/trip-requests",
        exchange -> {
          receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
          byte[] body =
              """
              [{"id":"%s","passengerId":"%s","originLabel":"UPC Monterrico","seatsRequested":1},\
              {"id":"%s","passengerId":"%s","originLabel":"UPC San Isidro","seatsRequested":2}]"""
                  .formatted(REQUEST_ID, PASSENGER_ID, UUID.randomUUID(), UUID.randomUUID())
                  .getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(status, body.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
          }
        });
    server.start();
  }

  @AfterEach
  void stopStub() {
    RequestContextHolder.resetRequestAttributes();
    server.stop(0);
  }

  @Test
  void findsTheTripRequestOfThePassenger() {
    assertThat(adapter().belongsTo(REQUEST_ID, PASSENGER_ID)).isTrue();
  }

  @Test
  void rejectsTheTripRequestOfAnotherPassenger() {
    assertThat(adapter().belongsTo(REQUEST_ID, UUID.randomUUID())).isFalse();
  }

  @Test
  void rejectsAnUnknownTripRequest() {
    assertThat(adapter().belongsTo(UUID.randomUUID(), PASSENGER_ID)).isFalse();
  }

  @Test
  void passesTheTokenOfTheCaller() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer passenger-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    adapter().belongsTo(REQUEST_ID, PASSENGER_ID);

    assertThat(receivedAuthorization.get()).isEqualTo("Bearer passenger-token");
  }

  @Test
  void reportsAnErrorAnswerAsUnavailable() {
    status = 500;

    assertThatThrownBy(() -> adapter().belongsTo(REQUEST_ID, PASSENGER_ID))
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessage("Trip Planning & Matching is not available");
  }

  @Test
  void reportsAServiceThatIsDownAsUnavailable() {
    String url = "http://127.0.0.1:" + server.getAddress().getPort();
    server.stop(0);

    assertThatThrownBy(
            () -> new HttpTripRequestsAdapter(url, 500).belongsTo(REQUEST_ID, PASSENGER_ID))
        .isInstanceOf(ServiceUnavailableException.class);
  }

  private HttpTripRequestsAdapter adapter() {
    return new HttpTripRequestsAdapter("http://127.0.0.1:" + server.getAddress().getPort(), 2000);
  }
}
