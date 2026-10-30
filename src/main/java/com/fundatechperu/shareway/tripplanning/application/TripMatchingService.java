package com.fundatechperu.shareway.tripplanning.application;

import com.fundatechperu.shareway.tripplanning.domain.model.DriverAvailability;
import com.fundatechperu.shareway.tripplanning.domain.model.MatchProposal;
import com.fundatechperu.shareway.tripplanning.domain.model.TripRequest;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.DriverAvailabilityRepository;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.MatchProposalRepository;
import com.fundatechperu.shareway.tripplanning.infrastructure.persistence.TripRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripMatchingService {

  private static final BigDecimal BASE_FARE = BigDecimal.valueOf(4.50);
  private static final BigDecimal PRICE_PER_SEAT = BigDecimal.valueOf(3.20);

  private final TripRequestRepository tripRequestRepository;
  private final DriverAvailabilityRepository driverAvailabilityRepository;
  private final MatchProposalRepository matchProposalRepository;

  public TripMatchingService(
      TripRequestRepository tripRequestRepository,
      DriverAvailabilityRepository driverAvailabilityRepository,
      MatchProposalRepository matchProposalRepository) {
    this.tripRequestRepository = tripRequestRepository;
    this.driverAvailabilityRepository = driverAvailabilityRepository;
    this.matchProposalRepository = matchProposalRepository;
  }

  @Transactional
  public TripRequest createTripRequest(TripRequestCommand command) {
    TripRequest tripRequest =
        new TripRequest(
            command.passengerId(),
            command.originLabel(),
            command.destinationLabel(),
            command.operationZone(),
            command.originLat(),
            command.originLng(),
            command.destinationLat(),
            command.destinationLng(),
            command.travelDate(),
            command.earliestDeparture(),
            command.latestDeparture(),
            command.seatsRequested());
    return tripRequestRepository.save(tripRequest);
  }

  @Transactional
  public DriverAvailability createDriverAvailability(DriverAvailabilityCommand command) {
    DriverAvailability availability =
        new DriverAvailability(
            command.driverId(),
            command.operationZone(),
            command.availableDate(),
            command.startTime(),
            command.endTime(),
            command.availableSeats());
    return driverAvailabilityRepository.save(availability);
  }

  @Transactional(readOnly = true)
  public List<TripRequest> listTripRequests() {
    return tripRequestRepository.findAllByOrderByCreatedAtDesc();
  }

  @Transactional
  public List<MatchProposal> generateMatchProposals(UUID tripRequestId) {
    List<MatchProposal> existingProposals =
        matchProposalRepository.findByTripRequestIdOrderByCreatedAtDesc(tripRequestId);
    if (!existingProposals.isEmpty()) {
      return deduplicateByDriverId(existingProposals);
    }

    TripRequest tripRequest =
        tripRequestRepository
            .findById(tripRequestId)
            .orElseThrow(
                () -> new EntityNotFoundException("Trip request not found: " + tripRequestId));

    List<DriverAvailability> candidates =
        driverAvailabilityRepository.findCandidates(
            tripRequest.getOperationZone(),
            tripRequest.getTravelDate(),
            tripRequest.getEarliestDeparture(),
            tripRequest.getLatestDeparture(),
            tripRequest.getSeatsRequested());

    Set<UUID> proposedDriverIds = new HashSet<>();
    List<MatchProposal> proposals =
        candidates.stream()
            .filter(candidate -> proposedDriverIds.add(candidate.getDriverId()))
            .limit(5)
            .map(
                candidate ->
                    new MatchProposal(
                        tripRequest,
                        candidate.getDriverId(),
                        estimateFare(tripRequest),
                        estimateDurationMinutes(tripRequest)))
            .toList();

    return matchProposalRepository.saveAll(proposals);
  }

  @Transactional(readOnly = true)
  public List<MatchProposal> listMatchProposals(UUID tripRequestId) {
    return deduplicateByDriverId(
        matchProposalRepository.findByTripRequestIdOrderByCreatedAtDesc(tripRequestId));
  }

  private BigDecimal estimateFare(TripRequest tripRequest) {
    return BASE_FARE
        .add(PRICE_PER_SEAT.multiply(BigDecimal.valueOf(tripRequest.getSeatsRequested())))
        .setScale(2, RoundingMode.HALF_UP);
  }

  private int estimateDurationMinutes(TripRequest tripRequest) {
    int windowMinutes =
        tripRequest.getLatestDeparture().toSecondOfDay()
            - tripRequest.getEarliestDeparture().toSecondOfDay();
    return Math.max(15, windowMinutes / 60);
  }

  private List<MatchProposal> deduplicateByDriverId(List<MatchProposal> proposals) {
    LinkedHashMap<UUID, MatchProposal> proposalsByDriver = new LinkedHashMap<>();
    for (MatchProposal proposal : proposals) {
      proposalsByDriver.putIfAbsent(proposal.getDriverId(), proposal);
    }
    return new ArrayList<>(proposalsByDriver.values());
  }
}
