package com.fundatechperu.shareway.tripplanning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fundatechperu.shareway.tripplanning.domain.model.DriverAvailability;
import com.fundatechperu.shareway.tripplanning.domain.model.MatchProposal;
import com.fundatechperu.shareway.tripplanning.domain.model.TripRequest;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.DriverAvailabilityRepository;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.MatchProposalRepository;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.TripRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TripMatchingServiceTest {

  private static final LocalDate TRAVEL_DATE = LocalDate.of(2026, 11, 3);

  private final TripRequestRepository tripRequests = mock(TripRequestRepository.class);
  private final DriverAvailabilityRepository availabilities =
      mock(DriverAvailabilityRepository.class);
  private final MatchProposalRepository proposals = mock(MatchProposalRepository.class);
  private final TripMatchingService service =
      new TripMatchingService(tripRequests, availabilities, proposals);

  private UUID tripRequestId;
  private TripRequest tripRequest;

  @BeforeEach
  void setUp() {
    tripRequestId = UUID.randomUUID();
    tripRequest =
        new TripRequest(
            UUID.randomUUID(),
            "UPC Monterrico",
            "Jockey Plaza",
            "Surco",
            new BigDecimal("-12.104"),
            new BigDecimal("-76.963"),
            new BigDecimal("-12.085"),
            new BigDecimal("-76.976"),
            TRAVEL_DATE,
            LocalTime.of(7, 0),
            LocalTime.of(7, 30),
            2);
    when(proposals.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void proposesEachCompatibleDriverOnceWithTheEstimatedFareAndDuration() {
    UUID driver = UUID.randomUUID();
    givenTripRequestAndCandidates(List.of(availability(driver), availability(driver)));

    List<MatchProposal> generated = service.generateMatchProposals(tripRequestId);

    assertThat(generated).hasSize(1);
    assertThat(generated.get(0).getDriverId()).isEqualTo(driver);
    // 4.50 + 3.20 per requested seat
    assertThat(generated.get(0).getEstimatedFare()).isEqualByComparingTo("10.90");
    // a 30-minute window is above the 15-minute floor
    assertThat(generated.get(0).getEstimatedDurationMinutes()).isEqualTo(30);
  }

  @Test
  void proposesAtMostFiveDrivers() {
    givenTripRequestAndCandidates(
        IntStream.range(0, 8).mapToObj(i -> availability(UUID.randomUUID())).toList());

    assertThat(service.generateMatchProposals(tripRequestId)).hasSize(5);
  }

  @Test
  void returnsTheExistingProposalsInsteadOfGeneratingNewOnes() {
    UUID driver = UUID.randomUUID();
    MatchProposal existing = new MatchProposal(tripRequest, driver, new BigDecimal("10.90"), 30);
    when(proposals.findByTripRequestIdOrderByCreatedAtDesc(tripRequestId))
        .thenReturn(List.of(existing, existing));

    assertThat(service.generateMatchProposals(tripRequestId)).containsExactly(existing);
    verify(availabilities, never()).findCandidates(any(), any(), any(), any(), anyInt());
  }

  @Test
  void unknownTripRequestIsNotFound() {
    when(proposals.findByTripRequestIdOrderByCreatedAtDesc(tripRequestId)).thenReturn(List.of());
    when(tripRequests.findById(tripRequestId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.generateMatchProposals(tripRequestId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(tripRequestId.toString());
  }

  private void givenTripRequestAndCandidates(List<DriverAvailability> candidates) {
    when(proposals.findByTripRequestIdOrderByCreatedAtDesc(tripRequestId)).thenReturn(List.of());
    when(tripRequests.findById(tripRequestId)).thenReturn(Optional.of(tripRequest));
    when(availabilities.findCandidates(
            "Surco", TRAVEL_DATE, LocalTime.of(7, 0), LocalTime.of(7, 30), 2))
        .thenReturn(candidates);
  }

  private static DriverAvailability availability(UUID driverId) {
    return new DriverAvailability(
        driverId, "Surco", TRAVEL_DATE, LocalTime.of(6, 30), LocalTime.of(8, 0), 3);
  }
}
