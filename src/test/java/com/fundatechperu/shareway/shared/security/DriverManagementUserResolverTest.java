package com.fundatechperu.shareway.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

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

/** Runs the resolver against a stub of the Driver Management {@code /api/v1/users/me} endpoint. */
class DriverManagementUserResolverTest {

  private static final String TOKEN = "Bearer stub-token";
  private static final AuthenticatedUser FROM_TOKEN =
      new AuthenticatedUser(
          UUID.fromString("7d9f3a52-5b8e-4c1a-9e2f-0a4b6c8d1e3f"),
          "ana.torres@upc.edu.pe",
          UserRole.PASSENGER);

  private final AtomicReference<String> receivedAuthorization = new AtomicReference<>();
  private HttpServer server;
  private int status;
  private String body;

  @BeforeEach
  void startStub() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext(
        "/api/v1/users/me",
        exchange -> {
          receivedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(status, bytes.length);
          try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
          }
        });
    server.start();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", TOKEN);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @AfterEach
  void stopStub() {
    RequestContextHolder.resetRequestAttributes();
    server.stop(0);
  }

  @Test
  void usesTheCurrentRoleReportedByDriverManagement() {
    answer(200, profile(FROM_TOKEN.id(), "DRIVER"));

    AuthenticatedUser user = resolver().resolve(FROM_TOKEN);

    assertThat(user.role()).isEqualTo(UserRole.DRIVER);
    assertThat(user.id()).isEqualTo(FROM_TOKEN.id());
    assertThat(receivedAuthorization.get()).isEqualTo(TOKEN);
  }

  @Test
  void keepsTheClaimsWhenTheServiceAnswersWithAnError() {
    answer(500, "{}");

    assertThat(resolver().resolve(FROM_TOKEN)).isEqualTo(FROM_TOKEN);
  }

  @Test
  void keepsTheClaimsWhenTheProfileBelongsToAnotherUser() {
    answer(200, profile(UUID.randomUUID(), "ADMIN"));

    assertThat(resolver().resolve(FROM_TOKEN)).isEqualTo(FROM_TOKEN);
  }

  @Test
  void keepsTheClaimsWhenTheServiceIsDown() {
    String url = "http://127.0.0.1:" + server.getAddress().getPort();
    server.stop(0);

    assertThat(new DriverManagementUserResolver(url, 500).resolve(FROM_TOKEN))
        .isEqualTo(FROM_TOKEN);
  }

  @Test
  void keepsTheClaimsOutsideAnHttpRequest() {
    RequestContextHolder.resetRequestAttributes();
    answer(200, profile(FROM_TOKEN.id(), "DRIVER"));

    assertThat(resolver().resolve(FROM_TOKEN)).isEqualTo(FROM_TOKEN);
    assertThat(receivedAuthorization.get()).isNull();
  }

  private DriverManagementUserResolver resolver() {
    return new DriverManagementUserResolver(
        "http://127.0.0.1:" + server.getAddress().getPort(), 2000);
  }

  private void answer(int status, String body) {
    this.status = status;
    this.body = body;
  }

  private static String profile(UUID id, String role) {
    return """
        {"id":"%s","email":"ana.torres@upc.edu.pe","fullName":"Ana Torres","phone":"987654321",\
        "role":"%s","driverStatus":null,"createdAt":"2026-10-30T10:00:00Z"}"""
        .formatted(id, role);
  }
}
