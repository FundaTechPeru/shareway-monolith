package com.fundatechperu.shareway.booking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fundatechperu.shareway.booking.application.dto.request.CreateBookingRequest;
import com.fundatechperu.shareway.booking.application.dto.response.BookingResponse;
import com.fundatechperu.shareway.booking.application.dto.response.CancellationResponse;
import com.fundatechperu.shareway.booking.application.service.BookingService;
import com.fundatechperu.shareway.booking.domain.exception.BookingCannotBeCancelledException;
import com.fundatechperu.shareway.booking.domain.exception.BookingNotFoundException;
import com.fundatechperu.shareway.booking.domain.exception.NoSeatsAvailableException;
import com.fundatechperu.shareway.booking.domain.model.BookingStatus;
import com.fundatechperu.shareway.booking.interfaces.BookingController;
import com.fundatechperu.shareway.shared.security.AuthenticatedUser;
import com.fundatechperu.shareway.shared.security.UserRole;
import com.fundatechperu.shareway.shared.web.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * HTTP contract of POST /api/v1/bookings and GET /api/v1/bookings/{id}: 202 + Location, 400, 404
 * and the 409 body with "El viaje ya no tiene asientos disponibles." The passenger is the
 * authenticated user; requests without a token are rejected by the filter chain before reaching the
 * controller (see BookingIntegrationTest).
 */
class BookingControllerTest {

  private static final String NO_SEATS_MESSAGE = "El viaje ya no tiene asientos disponibles.";
  private static final UUID PASSENGER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final String VALID_BODY =
      """
      {"groupId":"11111111-1111-1111-1111-111111111111",
      "requestId":"22222222-2222-2222-2222-222222222222"}""";

  private final BookingService bookingService = mock(BookingService.class);
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new BookingController(bookingService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    authenticateAs(PASSENGER_ID);
  }

  @AfterEach
  void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createReturns202WithLocationAndRequestedBody() throws Exception {
    UUID bookingId = UUID.randomUUID();
    when(bookingService.createBooking(
            any(CreateBookingRequest.class), eq(PASSENGER_ID), eq("idem-key-1")))
        .thenReturn(new BookingResponse(bookingId, BookingStatus.REQUESTED));

    mockMvc
        .perform(
            post("/api/v1/bookings")
                .header("Idempotency-Key", "idem-key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_BODY))
        .andExpect(status().isAccepted())
        .andExpect(header().string("Location", "/api/v1/bookings/" + bookingId))
        .andExpect(jsonPath("$.bookingId").value(bookingId.toString()))
        .andExpect(jsonPath("$.status").value("REQUESTED"));
  }

  @Test
  void createWithoutGroupIdIsA400AndNeverReachesTheService() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestId\":\"22222222-2222-2222-2222-222222222222\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    verifyNoInteractions(bookingService);
  }

  @Test
  void noSeatsIsA409WithTheExactMessage() throws Exception {
    when(bookingService.createBooking(any(), any(), any()))
        .thenThrow(new NoSeatsAvailableException());

    mockMvc
        .perform(
            post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(NO_SEATS_MESSAGE));
  }

  @Test
  void getExistingBookingReturns200WithUppercaseStatus() throws Exception {
    UUID bookingId = UUID.randomUUID();
    when(bookingService.findById(bookingId))
        .thenReturn(new BookingResponse(bookingId, BookingStatus.REQUESTED));

    mockMvc
        .perform(get("/api/v1/bookings/" + bookingId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingId").value(bookingId.toString()))
        .andExpect(jsonPath("$.status").value("REQUESTED"));
  }

  @Test
  void getUnknownBookingReturns404() throws Exception {
    UUID bookingId = UUID.randomUUID();
    when(bookingService.findById(bookingId)).thenThrow(new BookingNotFoundException(bookingId));

    mockMvc
        .perform(get("/api/v1/bookings/" + bookingId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").value("Booking not found: " + bookingId));
  }

  @Test
  void cancelByTheOwnerReturns200WithoutPenalty() throws Exception {
    UUID bookingId = UUID.randomUUID();
    when(bookingService.cancel(eq(bookingId), eq(PASSENGER_ID)))
        .thenReturn(new CancellationResponse(bookingId, BookingStatus.CANCELLED, null, null));

    mockMvc
        .perform(post("/api/v1/bookings/" + bookingId + "/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingId").value(bookingId.toString()))
        .andExpect(jsonPath("$.status").value("CANCELLED"))
        .andExpect(jsonPath("$.penaltyAmount").isEmpty())
        .andExpect(jsonPath("$.currency").isEmpty());
  }

  @Test
  void lateCancelReportsThePenaltyAmountAndCurrency() throws Exception {
    UUID bookingId = UUID.randomUUID();
    when(bookingService.cancel(eq(bookingId), eq(PASSENGER_ID)))
        .thenReturn(
            new CancellationResponse(
                bookingId, BookingStatus.CANCELLED, new BigDecimal("4.27"), "PEN"));

    mockMvc
        .perform(post("/api/v1/bookings/" + bookingId + "/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bookingId").value(bookingId.toString()))
        .andExpect(jsonPath("$.status").value("CANCELLED"))
        .andExpect(jsonPath("$.penaltyAmount").value(4.27))
        .andExpect(jsonPath("$.currency").value("PEN"));
  }

  @Test
  void cancellingABoardedBookingIsA409() throws Exception {
    UUID bookingId = UUID.randomUUID();
    when(bookingService.cancel(eq(bookingId), eq(PASSENGER_ID)))
        .thenThrow(
            new BookingCannotBeCancelledException(
                "La reserva ya no puede cancelarse en su estado actual: BOARDED"));

    mockMvc
        .perform(post("/api/v1/bookings/" + bookingId + "/cancel"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(
            jsonPath("$.message")
                .value("La reserva ya no puede cancelarse en su estado actual: BOARDED"));
  }

  @Test
  void cancelForAnotherPassengerIsA404() throws Exception {
    UUID bookingId = UUID.randomUUID();
    UUID strangerId = UUID.fromString("99999999-9999-9999-9999-999999999999");
    when(bookingService.cancel(eq(bookingId), eq(strangerId)))
        .thenThrow(new BookingNotFoundException(bookingId));
    authenticateAs(strangerId);

    mockMvc
        .perform(post("/api/v1/bookings/" + bookingId + "/cancel"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  private static void authenticateAs(UUID passengerId) {
    AuthenticatedUser passenger =
        new AuthenticatedUser(passengerId, "passenger@shareway.test", UserRole.PASSENGER);
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                passenger, null, passenger.authorities()));
  }
}
