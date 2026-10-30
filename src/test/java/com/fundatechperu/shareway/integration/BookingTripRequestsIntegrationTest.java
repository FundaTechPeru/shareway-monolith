package com.fundatechperu.shareway.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fundatechperu.shareway.booking.domain.service.PaymentsPort;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.JwtService;
import com.fundatechperu.shareway.shared.security.UserRole;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Booking &amp; Pricing checks the trip request of a booking in Trip Planning &amp; Matching, here
 * in process: only the passenger who asked for the trip request can book with it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingTripRequestsIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private JwtService jwtService;

  /** Payments &amp; Settlement is replaced: this test covers the trip request check alone. */
  @MockitoBean private PaymentsPort paymentsPort;

  private UUID groupId;

  @BeforeEach
  void setUp() {
    for (String table :
        new String[] {
          "bp.fares", "bp.penalties", "bp.booking_sagas", "bp.bookings", "bp.seat_inventories"
        }) {
      jdbcTemplate.update("DELETE FROM " + table);
    }
    groupId = UUID.randomUUID();
    jdbcTemplate.update(
        "INSERT INTO bp.seat_inventories (group_id, capacity, available_seats) VALUES (?, 4, 4)",
        groupId);
  }

  @Test
  void passengerBooksWithOwnTripRequest() throws Exception {
    UUID passengerId = UUID.randomUUID();
    UUID requestId = createTripRequest(passengerId);

    book(passengerId, requestId).andExpect(status().isAccepted());
  }

  @Test
  void unknownTripRequestIsNotFound() throws Exception {
    UUID requestId = UUID.randomUUID();

    book(UUID.randomUUID(), requestId)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Trip request not found: " + requestId));
  }

  @Test
  void tripRequestOfAnotherPassengerIsNotFound() throws Exception {
    UUID requestId = createTripRequest(UUID.randomUUID());

    book(UUID.randomUUID(), requestId).andExpect(status().isNotFound());
  }

  private UUID createTripRequest(UUID passengerId) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/trip-requests")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(passengerId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"passengerId":"%s","originLabel":"UPC Monterrico",\
                        "destinationLabel":"Jockey Plaza","operationZone":"Surco",\
                        "originLat":-12.104,"originLng":-76.963,"destinationLat":-12.085,\
                        "destinationLng":-76.976,"travelDate":"%s","earliestDeparture":"07:00",\
                        "latestDeparture":"07:30","seatsRequested":1}"""
                            .formatted(passengerId, LocalDate.now().plusDays(7))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return UUID.fromString(JsonPath.read(body, "$.id"));
  }

  private ResultActions book(UUID passengerId, UUID requestId) throws Exception {
    return mockMvc.perform(
        post("/api/v1/bookings")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token(passengerId))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"%s\",\"requestId\":\"%s\"}".formatted(groupId, requestId)));
  }

  private String token(UUID userId) {
    return jwtService
        .issue(new AuthenticatedUser(userId, "ana@upc.edu.pe", UserRole.PASSENGER))
        .value();
  }
}
