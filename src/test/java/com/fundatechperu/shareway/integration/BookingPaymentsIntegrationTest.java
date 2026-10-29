package com.fundatechperu.shareway.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fundatechperu.shareway.booking.infrastructure.provider.SimulatedDepartureTimeProvider;
import com.fundatechperu.shareway.payments.domain.model.CardBrand;
import com.fundatechperu.shareway.payments.domain.model.PaymentMethod;
import com.fundatechperu.shareway.payments.domain.repository.PaymentMethodRepository;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.JwtService;
import com.fundatechperu.shareway.shared.security.UserRole;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Booking &amp; Pricing and Payments &amp; Settlement talking through in-process events: a booking
 * asks for the payment authorization, the result moves the booking saga, and a cancellation voids
 * the authorization or charges the penalty.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingPaymentsIntegrationTest {

  /** Solo rider over the fake 12 km with fare rule version 1: (5 + 1.2 * 12) * 1.10. */
  private static final BigDecimal ESTIMATED_FARE = new BigDecimal("21.34");

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtService jwtService;
  @Autowired private PaymentMethodRepository paymentMethods;
  @Autowired private SimulatedDepartureTimeProvider departureTimeProvider;

  private UUID passengerId;
  private UUID groupId;

  @BeforeEach
  void setUp() {
    for (String table :
        new String[] {
          "bp.fares",
          "bp.penalties",
          "bp.booking_sagas",
          "bp.bookings",
          "bp.seat_inventories",
          "ps.penalties",
          "ps.payments",
          "ps.payment_methods"
        }) {
      jdbcTemplate.update("DELETE FROM " + table);
    }
    passengerId = UUID.randomUUID();
    groupId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO bp.seat_inventories (group_id, capacity, available_seats) VALUES (?, 4, 4)",
        groupId);
    departureTimeProvider.simulateOffsetMinutes(120);
  }

  @AfterEach
  void restoreDeparture() {
    departureTimeProvider.simulateOffsetMinutes(120);
  }

  @Test
  void bookingAuthorizesTheEstimatedFareAndAdvancesTheSaga() throws Exception {
    registerCard("tok_visa_ok");

    UUID bookingId = book();

    Map<String, Object> payment = paymentOf(bookingId);
    assertThat(payment.get("status")).isEqualTo("AUTHORIZED");
    assertThat((BigDecimal) payment.get("authorized_amount")).isEqualByComparingTo(ESTIMATED_FARE);
    assertThat(payment.get("passenger_id")).isEqualTo(passengerId);
    assertThat(sagaStep(bookingId)).isEqualTo("WAIT_GROUP");
  }

  @Test
  void declinedCardCompensatesTheBooking() throws Exception {
    registerCard("tok_declined");

    UUID bookingId = book();

    assertThat(paymentOf(bookingId).get("status")).isEqualTo("REJECTED");
    assertThat(bookingStatus(bookingId)).isEqualTo("cancelled");
    assertThat(availableSeats()).isEqualTo(4);
  }

  @Test
  void passengerWithoutCardGetsTheBookingCancelled() throws Exception {
    UUID bookingId = book();

    assertThat(bookingStatus(bookingId)).isEqualTo("cancelled");
    assertThat(sagaStatus(bookingId)).isEqualTo("compensated");
    assertThat(availableSeats()).isEqualTo(4);
  }

  @Test
  void earlyCancellationVoidsTheAuthorization() throws Exception {
    registerCard("tok_visa_ok");
    UUID bookingId = book();

    cancel(bookingId);

    assertThat(paymentOf(bookingId).get("status")).isEqualTo("VOIDED");
    assertThat(count("ps.penalties")).isZero();
  }

  @Test
  void lateCancellationChargesThePenalty() throws Exception {
    registerCard("tok_visa_ok");
    UUID bookingId = book();
    departureTimeProvider.simulateOffsetMinutes(10);

    cancel(bookingId);

    Map<String, Object> payment = paymentOf(bookingId);
    assertThat(payment.get("status")).isEqualTo("CAPTURED");
    // Payments & Settlement charges 50 % of the authorized amount.
    assertThat((BigDecimal) payment.get("captured_amount")).isEqualByComparingTo("10.67");
    assertThat(count("ps.penalties")).isEqualTo(1);
    // Booking & Pricing still records its own 20 % penalty (see Known issues in the README).
    assertThat(count("bp.penalties")).isEqualTo(1);
  }

  private void registerCard(String cardToken) {
    paymentMethods.save(
        PaymentMethod.register(
            passengerId, cardToken, CardBrand.VISA, "4242", 12, 2099, YearMonth.now()));
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

  private void cancel(UUID bookingId) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/bookings/" + bookingId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  private String token() {
    return jwtService
        .issue(new AuthenticatedUser(passengerId, "ana@upc.edu.pe", UserRole.PASSENGER))
        .value();
  }

  private Map<String, Object> paymentOf(UUID bookingId) {
    return jdbcTemplate.queryForMap("SELECT * FROM ps.payments WHERE booking_id = ?", bookingId);
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

  private int count(String table) {
    return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
  }
}
