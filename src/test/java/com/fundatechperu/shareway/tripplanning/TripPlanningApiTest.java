package com.fundatechperu.shareway.tripplanning;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.JwtService;
import com.fundatechperu.shareway.shared.security.UserRole;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TripPlanningApiTest {

  private static final UUID PASSENGER_ID = UUID.randomUUID();
  private static final UUID DRIVER_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtService jwtService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void emptyTripPlanningTables() {
    jdbcTemplate.update("DELETE FROM tpm.match_proposals");
    jdbcTemplate.update("DELETE FROM tpm.driver_availabilities");
    jdbcTemplate.update("DELETE FROM tpm.trip_requests");
  }

  @Test
  void requestsWithoutTokenAreUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/trip-requests")).andExpect(status().isUnauthorized());
  }

  @Test
  void createsAndListsTripRequests() throws Exception {
    mockMvc
        .perform(authorized(post("/api/v1/trip-requests")).content(tripRequestBody(2)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", startsWith("/api/v1/trip-requests/")))
        .andExpect(jsonPath("$.passengerId").value(PASSENGER_ID.toString()))
        .andExpect(jsonPath("$.status").value("PENDING_MATCH"));

    mockMvc
        .perform(authorized(get("/api/v1/trip-requests")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void invalidTripRequestIsRejected() throws Exception {
    mockMvc
        .perform(authorized(post("/api/v1/trip-requests")).content(tripRequestBody(0)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("seatsRequested"));
  }

  @Test
  void matchesACompatibleDriverIdempotently() throws Exception {
    mockMvc
        .perform(authorized(post("/api/v1/driver-availabilities")).content(availabilityBody()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.driverId").value(DRIVER_ID.toString()));
    String tripRequestId = createTripRequest();
    String path = "/api/v1/trip-requests/" + tripRequestId + "/match-proposals";

    mockMvc
        .perform(authorized(post(path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].driverId").value(DRIVER_ID.toString()))
        .andExpect(jsonPath("$[0].estimatedFare").value(10.9));
    mockMvc.perform(authorized(post(path))).andExpect(jsonPath("$", hasSize(1)));
    mockMvc
        .perform(authorized(get(path)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void unknownTripRequestIsNotFound() throws Exception {
    mockMvc
        .perform(
            authorized(post("/api/v1/trip-requests/" + UUID.randomUUID() + "/match-proposals")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void serviceInfoMovedUnderTheApiPrefix() throws Exception {
    mockMvc
        .perform(authorized(get("/api/v1/info")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi").value("/v3/api-docs/tripplanning"));
  }

  private String createTripRequest() throws Exception {
    String body =
        mockMvc
            .perform(authorized(post("/api/v1/trip-requests")).content(tripRequestBody(2)))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return JsonPath.read(body, "$.id");
  }

  private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder request) {
    String token =
        jwtService
            .issue(new AuthenticatedUser(PASSENGER_ID, "ana@upc.edu.pe", UserRole.PASSENGER))
            .value();
    return request
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON);
  }

  private static String tripRequestBody(int seats) {
    return """
        {
          "passengerId": "%s",
          "originLabel": "UPC Monterrico",
          "destinationLabel": "Jockey Plaza",
          "operationZone": "Surco",
          "originLat": -12.104,
          "originLng": -76.963,
          "destinationLat": -12.085,
          "destinationLng": -76.976,
          "travelDate": "2026-11-03",
          "earliestDeparture": "07:00",
          "latestDeparture": "07:30",
          "seatsRequested": %d
        }
        """
        .formatted(PASSENGER_ID, seats);
  }

  private static String availabilityBody() {
    return """
        {
          "driverId": "%s",
          "operationZone": "surco",
          "availableDate": "2026-11-03",
          "startTime": "06:30",
          "endTime": "08:00",
          "availableSeats": 3
        }
        """
        .formatted(DRIVER_ID);
  }
}
