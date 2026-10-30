package com.fundatechperu.shareway.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fundatechperu.shareway.booking.domain.service.TripRequestsPort;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.JwtService;
import com.fundatechperu.shareway.shared.security.UserRole;
import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Booking &amp; Pricing talking to an extracted Payments &amp; Settlement over HTTP: the service is
 * replaced by a stub that follows the contract of its UUID version (internal endpoints with {@code
 * X-Internal-Api-Key}, cards listed with the passenger token).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingPaymentsHttpIntegrationTest {

  private static final String API_KEY = "test-internal-api-key";
  private static final UUID CARD_ID = UUID.fromString("0f8e7d6c-5b4a-4392-8170-6f5e4d3c2b1a");
  private static final UUID PAYMENT_ID = UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");

  private static final PaymentsStub payments = PaymentsStub.start();

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtService jwtService;

  /** Every requestId counts as a trip request of the passenger. */
  @MockitoBean private TripRequestsPort tripRequestsPort;

  private UUID passengerId;
  private UUID groupId;

  @DynamicPropertySource
  static void paymentsService(DynamicPropertyRegistry registry) {
    registry.add("app.services.payments-settlement.url", payments::url);
  }

  @AfterAll
  static void stopStub() {
    payments.server.stop(0);
  }

  @BeforeEach
  void setUp() {
    for (String table :
        new String[] {
          "bp.fares", "bp.penalties", "bp.booking_sagas", "bp.bookings", "bp.seat_inventories"
        }) {
      jdbcTemplate.update("DELETE FROM " + table);
    }
    passengerId = UUID.randomUUID();
    groupId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO bp.seat_inventories (group_id, capacity, available_seats) VALUES (?, 4, 4)",
        groupId);
    payments.reset();
    given(tripRequestsPort.belongsTo(any(), any())).willReturn(true);
  }

  @Test
  void authorizedPaymentAdvancesTheSaga() throws Exception {
    payments.cards = "[{\"id\":\"%s\",\"brand\":\"VISA\",\"last4\":\"4242\"}]".formatted(CARD_ID);
    payments.authorization(201, "{\"id\":\"%s\",\"status\":\"AUTHORIZED\"}".formatted(PAYMENT_ID));

    UUID bookingId = book();

    assertThat(sagaStep(bookingId)).isEqualTo("WAIT_GROUP");
    Received cards = payments.only("GET /api/v1/payment-methods");
    assertThat(cards.authorization()).isEqualTo("Bearer " + token());
    Received authorization = payments.only("POST /api/v1/internal/payments/authorizations");
    assertThat(authorization.apiKey()).isEqualTo(API_KEY);
    assertThat(field(authorization, "$.bookingId")).isEqualTo(bookingId.toString());
    assertThat(field(authorization, "$.passengerId")).isEqualTo(passengerId.toString());
    assertThat(field(authorization, "$.paymentMethodId")).isEqualTo(CARD_ID.toString());
    assertThat(field(authorization, "$.currency")).isEqualTo("PEN");
    assertThat(new BigDecimal(JsonPath.read(authorization.body(), "$.amount").toString()))
        .isEqualByComparingTo("21.34");
  }

  @Test
  void declinedPaymentCompensatesTheBookingAndReportsTheCancellation() throws Exception {
    payments.cards = "[{\"id\":\"%s\"}]".formatted(CARD_ID);
    payments.authorization(402, "{\"status\":402,\"message\":\"Card declined\"}");

    UUID bookingId = book();

    assertThat(bookingStatus(bookingId)).isEqualTo("cancelled");
    assertThat(availableSeats()).isEqualTo(4);
    // PAYMENT_FAILED is a platform reason: Payments & Settlement voids without penalty.
    Received cancellation = payments.only("POST /api/v1/internal/events/booking-cancelled");
    assertThat(cancellation.apiKey()).isEqualTo(API_KEY);
    assertThat(field(cancellation, "$.bookingId")).isEqualTo(bookingId.toString());
    assertThat(field(cancellation, "$.reason")).isEqualTo("NO_DRIVER_AVAILABLE");
  }

  @Test
  void passengerWithoutCardGetsTheBookingCancelled() throws Exception {
    payments.cards = "[]";

    UUID bookingId = book();

    assertThat(bookingStatus(bookingId)).isEqualTo("cancelled");
    assertThat(payments.requests("POST /api/v1/internal/payments/authorizations")).isEmpty();
  }

  @Test
  void unavailableServiceCompensatesTheBooking() throws Exception {
    payments.cards = "[{\"id\":\"%s\"}]".formatted(CARD_ID);
    payments.authorization(503, "{\"status\":503,\"message\":\"Payment provider unavailable\"}");

    UUID bookingId = book();

    assertThat(bookingStatus(bookingId)).isEqualTo("cancelled");
    assertThat(sagaStatus(bookingId)).isEqualTo("compensated");
  }

  @Test
  void passengerCancellationIsReportedWithItsReason() throws Exception {
    payments.cards = "[{\"id\":\"%s\"}]".formatted(CARD_ID);
    payments.authorization(201, "{\"id\":\"%s\",\"status\":\"AUTHORIZED\"}".formatted(PAYMENT_ID));
    UUID bookingId = book();

    mockMvc
        .perform(
            post("/api/v1/bookings/" + bookingId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
        .andExpect(status().isOk());

    Received cancellation = payments.only("POST /api/v1/internal/events/booking-cancelled");
    assertThat(field(cancellation, "$.reason")).isEqualTo("PASSENGER_CANCELLED");
    assertThat(field(cancellation, "$.scheduledDepartureAt")).isNotBlank();
  }

  private UUID book() throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/bookings")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"groupId\":\"%s\",\"requestId\":\"%s\"}"
                            .formatted(groupId, UUID.randomUUID())))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(JsonPath.read(body, "$.bookingId"));
  }

  private String token() {
    return jwtService
        .issue(new AuthenticatedUser(passengerId, "ana@upc.edu.pe", UserRole.PASSENGER))
        .value();
  }

  private static String field(Received request, String path) {
    return JsonPath.read(request.body(), path).toString();
  }

  private String bookingStatus(UUID bookingId) {
    return jdbcTemplate.queryForObject(
        "SELECT status FROM bp.bookings WHERE booking_id = ?", String.class, bookingId);
  }

  private String sagaStep(UUID bookingId) {
    return jdbcTemplate.queryForObject(
        "SELECT current_step FROM bp.booking_sagas WHERE booking_id = ?", String.class, bookingId);
  }

  private String sagaStatus(UUID bookingId) {
    return jdbcTemplate.queryForObject(
        "SELECT status FROM bp.booking_sagas WHERE booking_id = ?", String.class, bookingId);
  }

  private int availableSeats() {
    return jdbcTemplate.queryForObject(
        "SELECT available_seats FROM bp.seat_inventories WHERE group_id = ?",
        Integer.class,
        groupId);
  }

  /** What the stub saw of one request. */
  record Received(String route, String authorization, String apiKey, String body) {}

  /** Stub of Payments &amp; Settlement on a random local port. */
  static final class PaymentsStub {

    private final HttpServer server;
    private final List<Received> received = new CopyOnWriteArrayList<>();
    private volatile String cards = "[]";
    private volatile int authorizationStatus = 201;
    private volatile String authorizationBody = "{}";
    private volatile int cancellationStatus = 409;

    private PaymentsStub(HttpServer server) {
      this.server = server;
    }

    static PaymentsStub start() {
      try {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        PaymentsStub stub = new PaymentsStub(server);
        server.createContext("/api/v1/payment-methods", ex -> stub.answer(ex, 200, stub.cards));
        server.createContext(
            "/api/v1/internal/payments/authorizations",
            ex -> stub.answer(ex, stub.authorizationStatus, stub.authorizationBody));
        server.createContext(
            "/api/v1/internal/events/booking-cancelled",
            ex ->
                stub.answer(
                    ex,
                    stub.cancellationStatus,
                    "{\"status\":409,\"message\":\"Not authorized\"}"));
        server.start();
        return stub;
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }

    String url() {
      return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    void reset() {
      received.clear();
      cards = "[]";
      authorization(201, "{}");
    }

    void authorization(int status, String body) {
      authorizationStatus = status;
      authorizationBody = body;
    }

    List<Received> requests(String route) {
      return received.stream().filter(request -> request.route().equals(route)).toList();
    }

    Received only(String route) {
      List<Received> matching = requests(route);
      assertThat(matching).as(route).hasSize(1);
      return matching.get(0);
    }

    private void answer(HttpExchange exchange, int status, String body) throws IOException {
      received.add(
          new Received(
              exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath(),
              exchange.getRequestHeaders().getFirst("Authorization"),
              exchange.getRequestHeaders().getFirst("X-Internal-Api-Key"),
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(status, bytes.length);
      try (OutputStream out = exchange.getResponseBody()) {
        out.write(bytes);
      }
    }
  }
}
